package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

// Интерфейс, объединяющий все хендлеры для HubEventProto-событий.
// Благодаря ему мы сможем внедрить все хендлеры в виде списка
// в компонент, который будет распределять получаемые события по
// их обработчикам
public interface HubEventHandler {

    void handle(HubEventProto event);
}