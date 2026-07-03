package ru.yandex.practicum.smarthometech.telemetry.analyzer.service;

import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

import java.util.Optional;

public interface SensorDataExtractor {
    Optional<Integer> extract(SensorStateAvro state);
}