package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.mapper.ScenarioAddedEventMapper;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.scenario.ScenarioAddedEvent;

@Slf4j
@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler implements HubEventHandler {

    protected ScenarioAddedEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Получено событие добавления сценария");
        ScenarioAddedEvent hubEvent = ScenarioAddedEventMapper.map(event);
        send(hubEvent);
    }
}