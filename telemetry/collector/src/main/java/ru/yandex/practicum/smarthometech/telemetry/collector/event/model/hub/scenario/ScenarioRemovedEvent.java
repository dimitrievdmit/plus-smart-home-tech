package ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.HubEventType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {
    @NotBlank
    @Length(min = 3, max = 2147483647)
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}
