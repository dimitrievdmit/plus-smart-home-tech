package ru.yandex.practicum.smarthometech.telemetry.collector.event.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.device.DeviceType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.ConditionOperation;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.ConditionType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.DeviceActionType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.device.DeviceAddedEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.device.DeviceRemovedEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.DeviceAction;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.ScenarioAddedEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.ScenarioCondition;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.ScenarioRemovedEvent;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class HubEventMapper {

    public static SpecificRecordBase mapToAvro(HubEvent event) {
        if (event == null) {
            return null;
        }

        Instant ts = event.getTimestamp() != null
                ? event.getTimestamp()
                : Instant.EPOCH;

        Object payload;
        switch (event.getType()) {
            case DEVICE_ADDED: {
                DeviceAddedEvent e = (DeviceAddedEvent) event;
                payload = DeviceAddedEventAvro.newBuilder()
                        .setId(e.getId())
                        .setType(mapDeviceType(e.getDeviceType()))
                        .build();
                break;
            }
            case DEVICE_REMOVED: {
                DeviceRemovedEvent e = (DeviceRemovedEvent) event;
                payload = DeviceRemovedEventAvro.newBuilder()
                        .setId(e.getId())
                        .build();
                break;
            }
            case SCENARIO_ADDED: {
                ScenarioAddedEvent e = (ScenarioAddedEvent) event;
                payload = ScenarioAddedEventAvro.newBuilder()
                        .setName(e.getName())
                        .setConditions(mapConditions(e.getConditions()))
                        .setActions(mapActions(e.getActions()))
                        .build();
                break;
            }
            case SCENARIO_REMOVED: {
                ScenarioRemovedEvent e = (ScenarioRemovedEvent) event;
                payload = ScenarioRemovedEventAvro.newBuilder()
                        .setName(e.getName())
                        .build();
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported hub event type: " + event.getType());
        }

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    // --- Enums mapping ---
    private static DeviceTypeAvro mapDeviceType(DeviceType type) {
        return DeviceTypeAvro.valueOf(type.name());
    }

    private static ActionTypeAvro mapActionType(DeviceActionType type) {
        return ActionTypeAvro.valueOf(type.name());
    }

    private static ConditionTypeAvro mapConditionType(ConditionType type) {
        return ConditionTypeAvro.valueOf(type.name());
    }

    private static ConditionOperationAvro mapConditionOperation(ConditionOperation op) {
        return ConditionOperationAvro.valueOf(op.name());
    }

    // --- Nested collections mapping ---
    private static List<ScenarioConditionAvro> mapConditions(List<ScenarioCondition> conditions) {
        if (conditions == null) {
            return null;
        }
        return conditions.stream()
                .map(HubEventMapper::mapCondition)
                .collect(Collectors.toList());
    }

    private static ScenarioConditionAvro mapCondition(ScenarioCondition c) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(c.getSensorId())
                .setType(mapConditionType(c.getType()))
                .setOperation(mapConditionOperation(c.getOperation()));

        builder.setValue(c.getValue());
        return builder.build();
    }

    private static List<DeviceActionAvro> mapActions(List<DeviceAction> actions) {
        if (actions == null) {
            return null;
        }
        return actions.stream()
                .map(HubEventMapper::mapAction)
                .collect(Collectors.toList());
    }

    private static DeviceActionAvro mapAction(DeviceAction a) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(a.getSensorId())
                .setType(mapActionType(a.getType()));

        builder.setValue(a.getValue());
        return builder.build();
    }
}