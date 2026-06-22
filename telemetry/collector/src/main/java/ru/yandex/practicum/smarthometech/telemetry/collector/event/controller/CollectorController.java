package ru.yandex.practicum.smarthometech.telemetry.collector.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.SensorEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.service.CollectorService;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CollectorController {
    private final CollectorService collectorService;

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        collectorService.collectSensorEvent(event);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent event) {
        collectorService.collectHubEvent(event);
    }

}
