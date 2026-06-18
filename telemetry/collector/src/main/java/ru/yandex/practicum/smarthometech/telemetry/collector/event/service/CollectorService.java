package ru.yandex.practicum.smarthometech.telemetry.collector.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.CollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.SensorEvent;

@SuppressWarnings({"unused"})
@Service
@Slf4j
public class CollectorService {
    private final CollectorStorage collectorStorage;

    public CollectorService(
            CollectorStorage collectorStorage
    ) {
        this.collectorStorage = collectorStorage;
    }

    public void collectSensorEvent(SensorEvent event) {
        collectorStorage.collectSensorEvent(event);
    }

    public void collectHubEvent(HubEvent event) {
        collectorStorage.collectHubEvent(event);
    }
}
