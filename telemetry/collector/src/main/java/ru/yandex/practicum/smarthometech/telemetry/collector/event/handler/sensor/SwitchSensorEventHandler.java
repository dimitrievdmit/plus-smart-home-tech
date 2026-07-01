package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaProtoSender;

@Component
public class SwitchSensorEventHandler extends BaseSensorEventHandler implements SensorEventHandler {

    public SwitchSensorEventHandler(KafkaProtoSender kafkaSender) {
        super(kafkaSender);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

}