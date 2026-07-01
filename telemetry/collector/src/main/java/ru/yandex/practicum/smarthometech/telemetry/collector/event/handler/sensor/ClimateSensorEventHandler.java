package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaProtoSender;

@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler implements SensorEventHandler {

    public ClimateSensorEventHandler(KafkaProtoSender kafkaSender) {
        super(kafkaSender);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

}