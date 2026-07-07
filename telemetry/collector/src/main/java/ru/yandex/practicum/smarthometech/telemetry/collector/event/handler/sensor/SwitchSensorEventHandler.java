package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.SwitchSensorEvent;

import java.time.Instant;

@Slf4j
@Component
public class SwitchSensorEventHandler extends BaseSensorEventHandler implements SensorEventHandler {

    protected SwitchSensorEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Получено событие датчика-переключателя");
        SwitchSensorProto proto = event.getSwitchSensor();

        SwitchSensorEvent sensorEvent = new SwitchSensorEvent();
        sensorEvent.setId(event.getId());
        sensorEvent.setHubId(event.getHubId());
        sensorEvent.setTimestamp(toInstant(event.getTimestamp()));
        sensorEvent.setState(proto.getState());

        send(sensorEvent);
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}