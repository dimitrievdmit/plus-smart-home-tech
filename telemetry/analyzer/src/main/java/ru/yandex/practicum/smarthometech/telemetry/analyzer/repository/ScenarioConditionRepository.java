package ru.yandex.practicum.smarthometech.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.entity.ScenarioCondition;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.entity.ScenarioConditionId;

import java.util.List;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {
    List<ScenarioCondition> findByIdScenarioId(Long scenarioId);

    void deleteByIdSensorId(String sensorId);
}