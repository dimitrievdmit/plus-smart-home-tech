package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.SensorEvent;

public abstract class BaseSensorEventHandler {

    private final KafkaCollectorStorage kafkaCollectorStorage;

    protected BaseSensorEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        this.kafkaCollectorStorage = kafkaCollectorStorage;
    }

    public void send(SensorEvent event) {
//        вызывает существующее хранилище, которое реализует маппинг из java модели в avro.
        kafkaCollectorStorage.collectSensorEvent(event);
    }
}
