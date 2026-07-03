package ru.yandex.practicum.smarthometech.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.entity.ScenarioAction;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.entity.ScenarioActionId;

import java.util.List;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {
    List<ScenarioAction> findByIdScenarioId(Long scenarioId);

    void deleteByIdSensorId(String sensorId);
}