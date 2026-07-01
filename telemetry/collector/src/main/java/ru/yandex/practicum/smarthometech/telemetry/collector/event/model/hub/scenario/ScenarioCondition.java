package ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.ConditionOperation;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.ConditionType;

@Getter
@Setter
public class ScenarioCondition {
    @NotBlank
    private String sensorId;
    @NotNull
    private ConditionType type;
    @NotNull
    private ConditionOperation operation;
    @NotNull
    private int value;
}
