package ru.yandex.practicum.smarthometech.telemetry.collector.event.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.hub.HubEventHandler;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.handler.sensor.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public EventController(Set<SensorEventHandler> sensorHandlers,
                           Set<HubEventHandler> hubHandlers) {
        // Преобразовываем набор хендлеров в map, где ключ — тип события от конкретного датчика или хаба.
        // Это нужно для упрощения поиска подходящего хендлера во время обработки событий
        this.sensorEventHandlers = sensorHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    /**
     * Метод для обработки событий от датчиков.
     * Вызывается при получении нового события от gRPC-клиента.
     *
     * @param request          Событие от датчика
     * @param responseObserver Ответ для клиента
     */
    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (sensorEventHandlers.containsKey(request.getPayloadCase())) {
                sensorEventHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для события " + request.getPayloadCase());
            }

            // после обработки события возвращаем ответ клиенту
            responseObserver.onNext(Empty.getDefaultInstance());
            // и завершаем обработку запроса
            responseObserver.onCompleted();
        } catch (Exception e) {
            // в случае исключения отправляем ошибку клиенту
            log.error("Ошибка обработки события датчиков {}: {}", request.getPayloadCase(), e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }

    /**
     * Метод для обработки событий от датчиков.
     * Вызывается при получении нового события от gRPC-клиента.
     *
     * @param request          Событие от датчика
     * @param responseObserver Ответ для клиента
     */
    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (hubEventHandlers.containsKey(request.getPayloadCase())) {
                hubEventHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для события " + request.getPayloadCase());
            }

            // после обработки события возвращаем ответ клиенту
            responseObserver.onNext(Empty.getDefaultInstance());
            // и завершаем обработку запроса
            responseObserver.onCompleted();
        } catch (Exception e) {
            // в случае исключения отправляем ошибку клиенту
            log.error("Ошибка обработки события хабов {}: {}", request.getPayloadCase(), e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}