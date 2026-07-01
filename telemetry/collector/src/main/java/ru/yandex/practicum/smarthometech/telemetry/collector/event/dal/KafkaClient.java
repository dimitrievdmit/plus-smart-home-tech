package ru.yandex.practicum.smarthometech.telemetry.collector.event.dal;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaClient {

    Producer<String, Message> getProducer();

    void stop();
}
