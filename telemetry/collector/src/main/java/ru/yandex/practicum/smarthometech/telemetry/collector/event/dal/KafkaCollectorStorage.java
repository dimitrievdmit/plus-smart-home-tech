package ru.yandex.practicum.smarthometech.telemetry.collector.event.dal;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.mapper.HubEventMapper;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.mapper.SensorEventMapper;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.hub.HubEvent;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.SensorEvent;

@Slf4j
@Repository
public class KafkaCollectorStorage implements CollectorStorage {

    private final KafkaClient client;

    public KafkaCollectorStorage(KafkaClient client) {
        this.client = client;
    }

    @Override
    public void collectSensorEvent(SensorEvent event) {
        client.getProducer().send(
                new ProducerRecord<>(
                        KafkaTopics.TELEMETRY_SENSORS_TOPIC,
                        SensorEventMapper.mapToAvro(event)
                )
        );
    }

    @Override
    public void collectHubEvent(HubEvent event) {
        client.getProducer().send(
                new ProducerRecord<>(
                        KafkaTopics.TELEMETRY_HUBS_TOPIC,
                        HubEventMapper.mapToAvro(event)
                )
        );
    }
}
