package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String serviceName) {
        super("Сервис " + serviceName + " временно недоступен. Пожалуйста, попробуйте позже.");
        log.info("Fallback активирован в связи с тем, что сервис {} недоступен", serviceName);
    }
}