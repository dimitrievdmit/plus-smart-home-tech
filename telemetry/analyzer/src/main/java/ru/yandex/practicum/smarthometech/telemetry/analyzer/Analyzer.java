package ru.yandex.practicum.smarthometech.telemetry.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.smarthometech.telemetry.analyzer.processor.SnapshotProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);

        HubEventProcessor hubEventProcessor = context.getBean(HubEventProcessor.class);
        SnapshotProcessor snapshotProcessor = context.getBean(SnapshotProcessor.class);

        Thread hubEventsThread = new Thread(hubEventProcessor, "HubEventHandlerThread");
        hubEventsThread.start();

        // основной поток – обработка снапшотов
        snapshotProcessor.start();

        try {
            hubEventsThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}