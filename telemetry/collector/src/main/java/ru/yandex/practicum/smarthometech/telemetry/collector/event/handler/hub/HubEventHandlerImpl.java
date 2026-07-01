package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaProtoSender;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaTopics;

@Component
public class HubEventHandlerImpl implements HubEventHandler {
    private final KafkaProtoSender kafkaSender;

    public HubEventHandlerImpl(KafkaProtoSender kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    @Override
    public void handle(HubEventProto event) {
        kafkaSender.send(KafkaTopics.TELEMETRY_HUBS_TOPIC, event);
    }
}
