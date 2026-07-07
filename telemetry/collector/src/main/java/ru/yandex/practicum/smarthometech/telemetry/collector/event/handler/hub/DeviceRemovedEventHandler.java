package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.device.DeviceRemovedEvent;

import java.time.Instant;

@Slf4j
@Component
public class DeviceRemovedEventHandler extends BaseHubEventHandler implements HubEventHandler {

    protected DeviceRemovedEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Получено событие удаления устройства");
        DeviceRemovedEventProto proto = event.getDeviceRemoved();

        DeviceRemovedEvent hubEvent = new DeviceRemovedEvent();
        hubEvent.setHubId(event.getHubId());
        hubEvent.setTimestamp(toInstant(event.getTimestamp()));
        hubEvent.setId(proto.getId());

        send(hubEvent);
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}