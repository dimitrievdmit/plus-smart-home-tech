package ru.yandex.practicum.smarthometech.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.entity.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}