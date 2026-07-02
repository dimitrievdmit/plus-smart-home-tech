package ru.yandex.practicum.smarthometech.telemetry.aggregator.serialization;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Exception e) {
        super(message, e);
    }
}