package ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.DeviceActionType;

@Getter
@Setter
public class DeviceAction {
    @NotBlank
    private String sensorId;
    @NotNull
    private DeviceActionType type;
    private Integer value;
}
