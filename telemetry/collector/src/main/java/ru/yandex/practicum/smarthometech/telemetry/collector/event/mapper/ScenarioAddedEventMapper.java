package ru.yandex.practicum.smarthometech.telemetry.collector.event.mapper;

import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.ConditionOperation;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.ConditionType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.scenario.DeviceActionType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.DeviceAction;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.ScenarioAddedEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.ScenarioCondition;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ScenarioAddedEventMapper {

    /**
     * Преобразует событие из Protobuf в доменную модель.
     *
     * @param eventProto protobuf-событие хаба
     * @return заполненный ScenarioAddedEvent
     */
    public static ScenarioAddedEvent map(HubEventProto eventProto) {
        ScenarioAddedEventProto proto = eventProto.getScenarioAdded();
        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setHubId(eventProto.getHubId());
        event.setTimestamp(mapTimestamp(eventProto.getTimestamp()));
        event.setName(proto.getName());
        event.setConditions(mapConditions(proto.getConditionList()));
        event.setActions(mapActions(proto.getActionList()));
        return event;
    }

    private static List<ScenarioCondition> mapConditions(List<ScenarioConditionProto> conditionProtos) {
        return conditionProtos.stream()
                .map(ScenarioAddedEventMapper::mapCondition)
                .collect(Collectors.toList());
    }

    private static ScenarioCondition mapCondition(ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(proto.getSensorId());
        condition.setType(mapConditionType(proto.getType()));
        condition.setOperation(mapConditionOperation(proto.getOperation()));
        condition.setValue(extractIntValue(proto));
        return condition;
    }

    private static int extractIntValue(ScenarioConditionProto proto) {
        return switch (proto.getValueCase()) {
            case BOOL_VALUE -> proto.getBoolValue() ? 1 : 0;
            case INT_VALUE -> proto.getIntValue();
            default ->
                // По умолчанию ставим 0; можно также бросить исключение
                    0;
        };
    }

    private static List<DeviceAction> mapActions(List<DeviceActionProto> actionProtos) {
        return actionProtos.stream()
                .map(ScenarioAddedEventMapper::mapAction)
                .collect(Collectors.toList());
    }

    private static DeviceAction mapAction(DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(proto.getSensorId());
        action.setType(mapActionType(proto.getType()));
        action.setValue(proto.hasValue() ? proto.getValue() : null);
        return action;
    }

    private static ConditionType mapConditionType(ConditionTypeProto proto) {
        return ConditionType.valueOf(proto.name());
    }

    private static ConditionOperation mapConditionOperation(ConditionOperationProto proto) {
        return ConditionOperation.valueOf(proto.name());
    }

    private static DeviceActionType mapActionType(ActionTypeProto proto) {
        return DeviceActionType.valueOf(proto.name());
    }

    private static Instant mapTimestamp(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}