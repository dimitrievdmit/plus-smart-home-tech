package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.ClimateSensorEvent;

import java.time.Instant;

@Slf4j
@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler implements SensorEventHandler {

    protected ClimateSensorEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Получено событие климатического датчика");
        ClimateSensorProto proto = event.getClimateSensor();

        ClimateSensorEvent sensorEvent = new ClimateSensorEvent();
        sensorEvent.setId(event.getId());
        sensorEvent.setHubId(event.getHubId());
        sensorEvent.setTimestamp(toInstant(event.getTimestamp()));
        sensorEvent.setTemperatureC(proto.getTemperatureC());
        sensorEvent.setHumidity(proto.getHumidity());
        sensorEvent.setCo2Level(proto.getCo2Level());

        send(sensorEvent);
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}