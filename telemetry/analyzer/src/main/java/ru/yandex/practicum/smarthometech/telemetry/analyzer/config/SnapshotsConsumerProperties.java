package ru.yandex.practicum.smarthometech.telemetry.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kafka.consumer.snapshots")
public class SnapshotsConsumerProperties {
    private String groupId;
    private String autoOffsetReset;
    private int maxPollRecords;
    private int commitBatchSize;
    private int pollTimeoutMillis;
}