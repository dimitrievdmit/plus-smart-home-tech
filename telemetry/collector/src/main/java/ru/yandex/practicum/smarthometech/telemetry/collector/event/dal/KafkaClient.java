package ru.yandex.practicum.smarthometech.telemetry.collector.event.dal;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaClient {

    Producer<String, SpecificRecordBase> getProducer();

    void stop();
}
