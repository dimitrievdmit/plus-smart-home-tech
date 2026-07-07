package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.LightSensorEvent;

import java.time.Instant;

@Slf4j
@Component
public class LightSensorEventHandler extends BaseSensorEventHandler implements SensorEventHandler {

    protected LightSensorEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Получено событие датчика освещённости");
        LightSensorProto proto = event.getLightSensor();

        LightSensorEvent sensorEvent = new LightSensorEvent();
        sensorEvent.setId(event.getId());
        sensorEvent.setHubId(event.getHubId());
        sensorEvent.setTimestamp(toInstant(event.getTimestamp()));
        sensorEvent.setLinkQuality(proto.getLinkQuality());
        sensorEvent.setLuminosity(proto.getLuminosity());

        send(sensorEvent);
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}