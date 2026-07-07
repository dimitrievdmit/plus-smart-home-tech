package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.dal.KafkaCollectorStorage;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.hub.device.DeviceType;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.device.DeviceAddedEvent;

import java.time.Instant;

@Slf4j
@Component
public class DeviceAddedEventHandler extends BaseHubEventHandler implements HubEventHandler {

    protected DeviceAddedEventHandler(KafkaCollectorStorage kafkaCollectorStorage) {
        super(kafkaCollectorStorage);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Получено событие добавления устройства");
        DeviceAddedEventProto proto = event.getDeviceAdded();

        DeviceAddedEvent hubEvent = new DeviceAddedEvent();
        hubEvent.setHubId(event.getHubId());
        hubEvent.setTimestamp(toInstant(event.getTimestamp()));
        hubEvent.setId(proto.getId());
        hubEvent.setDeviceType(mapDeviceType(proto.getType()));

        send(hubEvent);
    }

    private DeviceType mapDeviceType(DeviceTypeProto protoType) {
        return DeviceType.valueOf(protoType.name());
    }

    private Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}