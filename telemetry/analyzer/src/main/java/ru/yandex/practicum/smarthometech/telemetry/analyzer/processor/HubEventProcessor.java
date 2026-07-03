package ru.yandex.practicum.smarthometech.telemetry.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.config.KafkaTopicsProperties;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final HubEventService hubEventService;
    private final KafkaTopicsProperties topicsProperties;

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(5000);

    @Override
    public void run() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            List<String> topics = List.of(topicsProperties.getHubs());
            consumer.subscribe(topics);
            log.info("HubEventProcessor подписан на топики: {}", topics);

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(POLL_TIMEOUT);
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        hubEventService.process(record.value());
                    } catch (Exception e) {
                        log.error("Ошибка обработки HubEvent из хаба {}",
                                record.value().getHubId(), e);
                    }
                }
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Ошибка фиксации оффсетов HubEvent: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException e) {
            log.info("HubEventProcessor получил сигнал Wakeup");
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки HubEvent", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер HubEvent");
                consumer.close();
            }
        }
    }
}