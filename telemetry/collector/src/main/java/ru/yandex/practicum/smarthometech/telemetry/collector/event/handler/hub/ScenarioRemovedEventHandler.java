package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.ScenarioRemovedEvent;

import java.time.Instant;

@Slf4j
@Component
public class ScenarioRemovedEventHandler extends BaseHubEventHandler implements HubEventHandler {

    protected ScenarioRemovedEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Получено событие удаления сценария");
        ScenarioRemovedEventProto proto = event.getScenarioRemoved();

        ScenarioRemovedEvent hubEvent = new ScenarioRemovedEvent();
        hubEvent.setHubId(event.getHubId());
        hubEvent.setTimestamp(toInstant(event.getTimestamp()));
        hubEvent.setName(proto.getName());

        send(hubEvent);
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}