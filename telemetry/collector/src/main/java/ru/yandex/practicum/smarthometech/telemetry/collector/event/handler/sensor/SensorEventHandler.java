package ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

// Интерфейс, объединяющий все хендлеры для SensorEventProto-событий.
// Благодаря ему мы сможем внедрить все хендлеры в виде списка
// в компонент, который будет распределять получаемые события по
// их обработчикам
public interface SensorEventHandler {

    void handle(SensorEventProto event);
}