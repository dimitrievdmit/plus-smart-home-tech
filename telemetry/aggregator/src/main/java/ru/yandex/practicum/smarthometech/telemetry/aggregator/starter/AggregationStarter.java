package ru.yandex.practicum.smarthometech.telemetry.aggregator.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.smarthometech.telemetry.aggregator.config.KafkaTopics;
import ru.yandex.practicum.smarthometech.telemetry.aggregator.service.SnapshotAggregator;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final SnapshotAggregator snapshotAggregator;
    private final Producer<String, SensorsSnapshotAvro> producer;

    private final KafkaConsumer<Void, SensorEventAvro> consumer;

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);
    private final List<String> TOPICS = List.of(KafkaTopics.TELEMETRY_SENSORS_TOPIC);

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        try {
            // Добавляем shutdown hook для корректного завершения
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(TOPICS);
            log.info("Aggregator подписан на топики: {}", TOPICS);

            // Цикл опроса и обработки событий
            //noinspection InfiniteLoopStatement
            while (true) {
                ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(POLL_TIMEOUT);

                int count = 0;
                for (ConsumerRecord<Void, SensorEventAvro> record : records) {
                    processRecord(record);
                    // фиксируем оффсеты обработанных записей, если нужно
                    manageOffsets(record, count, consumer);
                    count++;
                }
                // фиксируем максимальный оффсет обработанных записей
                consumer.commitAsync();
            }

        } catch (WakeupException ignores) {
            log.info("Получен сигнал завершения (Wakeup)");
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы
                producer.flush();
                consumer.commitSync(currentOffsets);

            } finally {
                log.info("Закрываем продюсер");
                producer.close();
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private void processRecord(ConsumerRecord<Void, SensorEventAvro> record) {
        SensorEventAvro event = record.value();
        log.info("Получено событие датчика: hub={}, sensor={}, type={}",
                event.getHubId(), event.getId(), event.getPayload().getClass().getSimpleName());

        Optional<SensorsSnapshotAvro> updatedSnapshot = snapshotAggregator.updateState(event);

        if (updatedSnapshot.isPresent()) {
            SensorsSnapshotAvro snapshot = updatedSnapshot.get();
            // Ключ - hubId, значение - снапшот
            ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
                    new ProducerRecord<>(KafkaTopics.TELEMETRY_SNAPSHOTS_TOPIC, snapshot.getHubId(), snapshot);
            producer.send(producerRecord, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки снапшота для хаба {}: {}", snapshot.getHubId(), exception.getMessage());
                } else {
                    log.info("Снапшот для хаба {} отправлен в топик {}", snapshot.getHubId(), metadata.topic());
                }
            });
        }
    }

    private void manageOffsets(
            ConsumerRecord<Void, SensorEventAvro> record,
            int count,
            KafkaConsumer<Void, SensorEventAvro> consumer
    ) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}
