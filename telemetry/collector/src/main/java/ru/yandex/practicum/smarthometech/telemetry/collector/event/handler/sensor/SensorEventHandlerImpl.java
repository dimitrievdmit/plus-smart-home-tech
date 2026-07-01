package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaProtoSender;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaTopics;

@Component
public class SensorEventHandlerImpl implements SensorEventHandler {
    private final KafkaProtoSender kafkaSender;

    public SensorEventHandlerImpl(KafkaProtoSender kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    @Override
    public void handle(SensorEventProto event) {
        kafkaSender.send(KafkaTopics.TELEMETRY_SENSORS_TOPIC, event);
    }
}
