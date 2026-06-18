package ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.HubEventType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.device.DeviceType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    @NotBlank
    private String id;
    @NotNull
    private DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}
