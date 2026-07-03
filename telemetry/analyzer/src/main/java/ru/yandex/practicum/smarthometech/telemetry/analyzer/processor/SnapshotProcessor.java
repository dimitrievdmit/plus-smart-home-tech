package ru.yandex.practicum.smarthometech.telemetry.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.config.KafkaTopicsProperties;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.service.ScenarioAnalyzer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final ScenarioAnalyzer analyzer;
    private final KafkaTopicsProperties topicsProperties;

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);
    private static final int COMMIT_BATCH_SIZE = 50;

    public void start() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            List<String> topics = List.of(topicsProperties.getSnapshots());
            consumer.subscribe(topics);
            log.info("SnapshotProcessor подписан на топики: {}", topics);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(POLL_TIMEOUT);
                if (records.isEmpty()) continue;

                List<ConsumerRecord<String, SensorsSnapshotAvro>> recordList = new ArrayList<>();
                records.forEach(recordList::add);

                for (int i = 0; i < recordList.size(); i += COMMIT_BATCH_SIZE) {
                    int end = Math.min(i + COMMIT_BATCH_SIZE, recordList.size());
                    List<ConsumerRecord<String, SensorsSnapshotAvro>> batch = recordList.subList(i, end);
                    processBatch(batch);
                }
            }
        } catch (WakeupException e) {
            log.info("SnapshotProcessor получил сигнал Wakeup");
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки снапшотов", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер снапшотов");
                consumer.close();
            }
        }
    }

    private void processBatch(List<ConsumerRecord<String, SensorsSnapshotAvro>> batch) {
        // Вычисляем оффсет для коммита (последняя запись + 1)
        long commitOffset = batch.getLast().offset() + 1;
        TopicPartition partition = new TopicPartition(
                batch.getFirst().topic(), batch.getFirst().partition()
        );
        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit =
                Map.of(partition, new OffsetAndMetadata(commitOffset));

        // Асинхронный коммит перед обработкой подгруппы для at-most-once доставки
        consumer.commitAsync(offsetsToCommit, (offsets, exception) -> {
            if (exception != null) {
                log.warn("Ошибка коммита подгруппы: {}", offsets, exception);
            } else {
                log.debug("Зафиксирован оффсет {} для партиции {}", commitOffset, partition);
            }
        });

        // Обрабатываем все записи подгруппы
        for (ConsumerRecord<String, SensorsSnapshotAvro> record : batch) {
            try {
                analyzer.analyze(record.value());
            } catch (Exception e) {
                log.error("Ошибка анализа снапшота из хаба {}", record.value().getHubId(), e);
            }
        }
    }
}