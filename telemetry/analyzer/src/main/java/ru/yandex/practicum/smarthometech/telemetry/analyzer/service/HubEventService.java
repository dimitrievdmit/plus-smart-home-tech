package ru.yandex.practicum.smarthometech.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.entity.*;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.repository.*;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Transactional
    public void process(HubEventAvro event) {
        if (event.getPayload() instanceof DeviceAddedEventAvro added) {
            handleDeviceAdded(event.getHubId(), added);
        } else if (event.getPayload() instanceof DeviceRemovedEventAvro removed) {
            handleDeviceRemoved(event.getHubId(), removed);
        } else if (event.getPayload() instanceof ScenarioAddedEventAvro added) {
            handleScenarioAdded(event.getHubId(), added);
        } else if (event.getPayload() instanceof ScenarioRemovedEventAvro removed) {
            handleScenarioRemoved(event.getHubId(), removed);
        } else {
            log.warn("Неизвестный тип события хаба: {}", event.getPayload().getClass());
        }
    }

    // ================== Device handlers (без изменений) ==================
    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        String sensorId = event.getId();
        if (sensorRepository.existsById(sensorId)) {
            log.debug("Датчик {} уже существует", sensorId);
            return;
        }
        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
        log.info("Добавлен датчик {} в хаб {}", sensorId, hubId);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
        String sensorId = event.getId();
        if (!sensorRepository.existsById(sensorId)) {
            log.debug("Датчик {} не найден для удаления", sensorId);
            return;
        }
        // Удаляем все связи, где фигурирует этот датчик
        scenarioConditionRepository.deleteByIdSensorId(sensorId);
        scenarioActionRepository.deleteByIdSensorId(sensorId);
        sensorRepository.deleteById(sensorId);
        log.info("Датчик {} и его связи удалены", sensorId);
    }

    // ================== Scenario handlers ==================
    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        String scenarioName = event.getName();
        // Находим или создаём сценарий
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName)
                .orElseGet(() -> {
                    Scenario s = new Scenario();
                    s.setHubId(hubId);
                    s.setName(scenarioName);
                    return s;
                });

        // Сохраняем сценарий, чтобы получить ID (если он был новым)
        scenario = scenarioRepository.saveAndFlush(scenario);

        // Если сценарий уже существовал, удаляем старые связи
        if (scenario.getId() != null) {
            List<ScenarioCondition> oldConditions = scenarioConditionRepository.findByIdScenarioId(scenario.getId());
            if (!oldConditions.isEmpty()) {
                scenarioConditionRepository.deleteAll(oldConditions);
            }
            List<ScenarioAction> oldActions = scenarioActionRepository.findByIdScenarioId(scenario.getId());
            if (!oldActions.isEmpty()) {
                scenarioActionRepository.deleteAll(oldActions);
            }
        }

        // Обрабатываем условия
        for (ScenarioConditionAvro condAvro : event.getConditions()) {
            Sensor sensor = sensorRepository.findByIdAndHubId(condAvro.getSensorId(), hubId).orElse(null);
            if (sensor == null) {
                log.warn("Датчик {} не найден в хабе {}, условие пропущено", condAvro.getSensorId(), hubId);
                continue;
            }
            Condition cond = createCondition(condAvro);
            cond = conditionRepository.saveAndFlush(cond);

            ScenarioConditionId scId = new ScenarioConditionId(scenario.getId(), sensor.getId(), cond.getId());
            ScenarioCondition sc = new ScenarioCondition();
            sc.setId(scId);
            scenarioConditionRepository.save(sc);
        }

        // Обрабатываем действия
        for (DeviceActionAvro actionAvro : event.getActions()) {
            Sensor sensor = sensorRepository.findByIdAndHubId(actionAvro.getSensorId(), hubId).orElse(null);
            if (sensor == null) {
                log.warn("Датчик {} не найден в хабе {}, действие пропущено", actionAvro.getSensorId(), hubId);
                continue;
            }
            Action action = createAction(actionAvro);
            action = actionRepository.saveAndFlush(action);

            ScenarioActionId saId = new ScenarioActionId(scenario.getId(), sensor.getId(), action.getId());
            ScenarioAction sa = new ScenarioAction();
            sa.setId(saId);
            scenarioActionRepository.save(sa);
        }

        log.info("Сценарий '{}' добавлен/обновлён в хабе {}", scenarioName, hubId);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        String scenarioName = event.getName();
        scenarioRepository.findByHubIdAndName(hubId, scenarioName).ifPresent(scenario -> {
            // Удаляем связи
            List<ScenarioCondition> conditions = scenarioConditionRepository.findByIdScenarioId(scenario.getId());
            scenarioConditionRepository.deleteAll(conditions);
            List<ScenarioAction> actions = scenarioActionRepository.findByIdScenarioId(scenario.getId());
            scenarioActionRepository.deleteAll(actions);
            scenarioRepository.delete(scenario);
            log.info("Сценарий '{}' удалён из хаба {}", scenarioName, hubId);
        });
        // Если сценарий не найден – ничего не делаем (идемпотентность)
    }

    // ================== Helper methods ==================
    private Condition createCondition(ScenarioConditionAvro avro) {
        Condition cond = new Condition();
        cond.setType(avro.getType().name());
        cond.setOperation(avro.getOperation().name());

        Object raw = avro.getValue();
        if (raw instanceof Boolean boolVal) {
            cond.setValue(boolVal ? 1 : 0);
        } else if (raw instanceof Integer intVal) {
            cond.setValue(intVal);
        } else {
            cond.setValue(null); // хотя по схеме значение всегда есть
        }
        return cond;
    }

    private Action createAction(DeviceActionAvro avro) {
        Action action = new Action();
        action.setType(avro.getType().name());
        action.setValue(avro.getValue()); // может быть null для некоторых типов
        return action;
    }
}