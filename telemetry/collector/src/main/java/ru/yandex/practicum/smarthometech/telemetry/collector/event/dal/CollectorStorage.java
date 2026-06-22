package ru.yandex.practicum.smarthometech.telemetry.collector.event.dal;

import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.SensorEvent;

public interface CollectorStorage {
    void collectSensorEvent(SensorEvent event);

    void collectHubEvent(HubEvent event);
}
