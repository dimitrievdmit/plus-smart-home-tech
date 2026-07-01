package ru.yandex.practicum.smarthometech.telemetry.collector.event.dal;

import com.google.protobuf.Message;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaProtobufSerializer implements Serializer<Message> {
    @Override
    public byte[] serialize(String topic, Message data) {
        return data == null ? null : data.toByteArray();
    }
}