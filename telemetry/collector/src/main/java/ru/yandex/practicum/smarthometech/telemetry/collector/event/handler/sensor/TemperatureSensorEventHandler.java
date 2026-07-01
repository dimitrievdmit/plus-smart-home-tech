package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.TemperatureSensorEvent;

import java.time.Instant;

@Slf4j
@Component
public class TemperatureSensorEventHandler extends BaseSensorEventHandler implements SensorEventHandler {

    protected TemperatureSensorEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Получено событие температурного датчика");
        TemperatureSensorProto proto = event.getTemperatureSensor();

        TemperatureSensorEvent sensorEvent = new TemperatureSensorEvent();
        sensorEvent.setId(event.getId());
        sensorEvent.setHubId(event.getHubId());
        sensorEvent.setTimestamp(toInstant(event.getTimestamp()));
        sensorEvent.setTemperatureC(proto.getTemperatureC());
        sensorEvent.setTemperatureF(proto.getTemperatureF());

        send(sensorEvent);
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}