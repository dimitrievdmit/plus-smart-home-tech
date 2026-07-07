package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub;

import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;

public abstract class BaseHubEventHandler {

    private final KafkaCollectorStorage kafkaCollectorStorage;

    protected BaseHubEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        this.kafkaCollectorStorage = kafkaCollectorStorage;
    }

    public void send(HubEvent event) {
        kafkaCollectorStorage.collectHubEvent(event);
    }
}