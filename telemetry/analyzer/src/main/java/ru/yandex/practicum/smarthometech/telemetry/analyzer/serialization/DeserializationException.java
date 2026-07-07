package ru.yandex.practicum.smarthometech.telemetry.analyzer.serialization;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Exception e) {
        super(message, e);
    }
}