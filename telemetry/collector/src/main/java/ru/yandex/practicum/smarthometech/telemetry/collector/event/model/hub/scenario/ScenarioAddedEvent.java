package ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.HubEventType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {
    @NotBlank
    @Length(min = 3, max = 2147483647)
    private String name;

    @NotNull
    private List<ScenarioCondition> conditions;

    @NotNull
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
