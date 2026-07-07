package ru.yandex.practicum.smarthometech.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SnapshotAggregator {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    /**
     * Обновляет снапшот данными из события.
     *
     * @return обновлённый снапшот, если состояние изменилось; иначе Optional.empty()
     */
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        if (event == null) {
            return Optional.empty();
        }

        String hubId = event.getHubId();
        String sensorId = event.getId();
        Instant eventTimestamp = event.getTimestamp() != null
                ? event.getTimestamp()
                : Instant.EPOCH;
        SpecificRecordBase eventPayload = (SpecificRecordBase) event.getPayload();

        // Получаем или создаём снапшот для хаба
        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, id -> {
            SensorsSnapshotAvro newSnapshot = new SensorsSnapshotAvro();
            newSnapshot.setHubId(id);
            newSnapshot.setTimestamp(Instant.EPOCH);
            newSnapshot.setSensorsState(new HashMap<>());
            return newSnapshot;
        });

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);

        // Если состояние уже есть и оно актуальнее или такое же - игнорируем
        if (oldState != null) {
            // Сравнение временных меток: если старые данные новее события, выходим
            if (oldState.getTimestamp() != null && oldState.getTimestamp().isAfter(eventTimestamp)) {
                log.info("Событие для датчика {} проигнорировано (старое состояние новее)", sensorId);
                return Optional.empty();
            }
            // Если данные полностью идентичны, обновление не требуется
            if (oldState.getData() != null && oldState.getData().equals(eventPayload)) {
                log.info("Событие для датчика {} не изменилось", sensorId);
                return Optional.empty();
            }
        }

        // Создаём новое состояние датчика
        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(eventTimestamp);
        newState.setData(eventPayload);

        // Обновляем снапшот
        sensorsState.put(sensorId, newState);
        snapshot.setTimestamp(eventTimestamp);
        snapshot.setSensorsState(sensorsState);

        log.info("Снапшот для хаба {} обновлён: датчик {} ({})", hubId, sensorId, eventPayload.getClass().getSimpleName());
        return Optional.of(snapshot);
    }
}
