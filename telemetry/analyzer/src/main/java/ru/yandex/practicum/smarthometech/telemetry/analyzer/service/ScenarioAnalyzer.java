package ru.yandex.practicum.smarthometech.telemetry.analyzer.service;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.entity.*;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.enums.ConditionOperation;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.enums.ConditionType;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScenarioAnalyzer {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    private final Map<ConditionType, SensorDataExtractor> extractors = Map.of(
            ConditionType.MOTION, state -> Optional.ofNullable(state.getData())
                    .filter(MotionSensorAvro.class::isInstance)
                    .map(MotionSensorAvro.class::cast)
                    .map(m -> m.getMotion() ? 1 : 0),
            ConditionType.LUMINOSITY, state -> Optional.ofNullable(state.getData())
                    .filter(LightSensorAvro.class::isInstance)
                    .map(LightSensorAvro.class::cast)
                    .map(LightSensorAvro::getLuminosity),
            ConditionType.SWITCH, state -> Optional.ofNullable(state.getData())
                    .filter(SwitchSensorAvro.class::isInstance)
                    .map(SwitchSensorAvro.class::cast)
                    .map(s -> s.getState() ? 1 : 0),
            ConditionType.TEMPERATURE, state -> {
                Object data = state.getData();
                if (data instanceof TemperatureSensorAvro t) {
                    return Optional.of(t.getTemperatureC());
                } else if (data instanceof ClimateSensorAvro c) {
                    return Optional.of(c.getTemperatureC());
                }
                return Optional.empty();
            },
            ConditionType.CO2LEVEL, state -> Optional.ofNullable(state.getData())
                    .filter(ClimateSensorAvro.class::isInstance)
                    .map(ClimateSensorAvro.class::cast)
                    .map(ClimateSensorAvro::getCo2Level),
            ConditionType.HUMIDITY, state -> Optional.ofNullable(state.getData())
                    .filter(ClimateSensorAvro.class::isInstance)
                    .map(ClimateSensorAvro.class::cast)
                    .map(ClimateSensorAvro::getHumidity)
    );

    /**
     * Анализирует снапшот и выполняет подходящие сценарии.
     */
    public void analyze(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.debug("Нет сценариев для хаба {}", hubId);
            return;
        }

        Map<String, SensorStateAvro> states = snapshot.getSensorsState();
        if (states == null || states.isEmpty()) {
            log.debug("Снапшот для хаба {} не содержит состояний датчиков", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            // Загружаем условия и действия для сценария через репозитории
            List<ScenarioCondition> scenarioConditions = scenarioConditionRepository.findByIdScenarioId(scenario.getId());

            // Формируем общий предикат для всех условий сценария
            Predicate<SensorsSnapshotAvro> predicate = buildPredicate(scenarioConditions, states);
            if (predicate.test(snapshot)) {
                log.info("Сценарий '{}' для хаба {} активирован", scenario.getName(), hubId);
                List<ScenarioAction> scenarioActions = scenarioActionRepository.findByIdScenarioId(scenario.getId());
                for (ScenarioAction sa : scenarioActions) {
                    // Загружаем действие по его идентификатору
                    Action action = actionRepository.findById(sa.getId().getActionId()).orElse(null);
                    if (action == null) {
                        log.warn("Действие с id {} не найдено", sa.getId().getActionId());
                        continue;
                    }
                    String sensorId = sa.getId().getSensorId();
                    executeAction(hubId, scenario.getName(), action, sensorId);
                }
            }
        }
    }

    /**
     * Строит предикат на основе списка условий.
     */
    private Predicate<SensorsSnapshotAvro> buildPredicate(List<ScenarioCondition> conditions,
                                                          Map<String, SensorStateAvro> states) {
        return conditions.stream()
                .map(sc -> toPredicate(sc, states))
                .reduce(Predicate::and)
                .orElse(s -> true);
    }

    /**
     * Преобразует одно условие в предикат.
     */
    private Predicate<SensorsSnapshotAvro> toPredicate(ScenarioCondition sc,
                                                       Map<String, SensorStateAvro> states) {
        // Загружаем Condition по идентификатору
        Condition cond = conditionRepository.findById(sc.getId().getConditionId()).orElse(null);
        if (cond == null) {
            log.warn("Условие с id {} не найдено", sc.getId().getConditionId());
            return s -> false;
        }

        ConditionType type = ConditionType.valueOf(cond.getType());
        ConditionOperation op = ConditionOperation.valueOf(cond.getOperation());
        int expected = cond.getValue();
        String sensorId = sc.getId().getSensorId();

        SensorStateAvro state = states.get(sensorId);
        if (state == null) {
            return s -> false;
        }

        SensorDataExtractor extractor = extractors.get(type);
        Optional<Integer> actualOpt = extractor.extract(state);
        if (actualOpt.isEmpty()) {
            return s -> false;
        }
        int actual = actualOpt.get();

        // Возвращаем предикат, который сравнивает фактическое значение с ожидаемым
        return s -> op.evaluate(actual, expected);
    }

    /**
     * Отправляет действие на выполнение через gRPC.
     */
    private void executeAction(String hubId, String scenarioName, Action action, String sensorId) {
        try {
            DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(ActionTypeProto.valueOf(action.getType()));
            if (action.getValue() != null) {
                actionBuilder.setValue(action.getValue());
            }

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(actionBuilder.build())
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano()))
                    .build();

            //noinspection ResultOfMethodCallIgnored
            hubRouterClient.handleDeviceAction(request);
            log.debug("Действие {} отправлено для сценария '{}'", action.getType(), scenarioName);
        } catch (StatusRuntimeException e) {
            log.error("Ошибка отправки действия для сценария '{}': {}", scenarioName, e.getMessage());
        }
    }
}