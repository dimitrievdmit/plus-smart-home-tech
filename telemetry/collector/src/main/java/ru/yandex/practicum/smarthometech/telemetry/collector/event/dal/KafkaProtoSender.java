package ru.yandex.practicum.smarthometech.telemetry.collector.event.dal;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

@Service
public class KafkaProtoSender {
    private final KafkaClient kafkaClient;

    public KafkaProtoSender(KafkaClient kafkaClient) {
        this.kafkaClient = kafkaClient;
    }

    public void send(String topic, Message message) {
        kafkaClient.getProducer().send(new ProducerRecord<>(topic, message));
    }
}