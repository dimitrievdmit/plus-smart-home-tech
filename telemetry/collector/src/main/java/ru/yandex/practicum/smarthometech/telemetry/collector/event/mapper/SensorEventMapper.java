package ru.yandex.practicum.smarthometech.telemetry.collector.event.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor.*;

import java.time.Instant;

public class SensorEventMapper {

    public static SpecificRecordBase mapToAvro(SensorEvent event) {
        if (event == null) {
            return null;
        }

        Instant ts = event.getTimestamp() != null
                ? event.getTimestamp()
                : Instant.EPOCH;

        Object payload;
        switch (event.getType()) {
            case CLIMATE_SENSOR_EVENT: {
                ClimateSensorEvent e = (ClimateSensorEvent) event;
                payload = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(e.getTemperatureC())
                        .setHumidity(e.getHumidity())
                        .setCo2Level(e.getCo2Level())
                        .build();
                break;
            }
            case LIGHT_SENSOR_EVENT: {
                LightSensorEvent e = (LightSensorEvent) event;
                payload = LightSensorAvro.newBuilder()
                        .setLinkQuality(e.getLinkQuality())
                        .setLuminosity(e.getLuminosity())
                        .build();
                break;
            }
            case MOTION_SENSOR_EVENT: {
                MotionSensorEvent e = (MotionSensorEvent) event;
                payload = MotionSensorAvro.newBuilder()
                        .setLinkQuality(e.getLinkQuality())
                        .setMotion(e.isMotion())
                        .setVoltage(e.getVoltage())
                        .build();
                break;
            }
            case SWITCH_SENSOR_EVENT: {
                SwitchSensorEvent e = (SwitchSensorEvent) event;
                payload = SwitchSensorAvro.newBuilder()
                        .setState(e.isState())
                        .build();
                break;
            }
            case TEMPERATURE_SENSOR_EVENT: {
                TemperatureSensorEvent e = (TemperatureSensorEvent) event;
                payload = TemperatureSensorAvro.newBuilder()
                        .setId(e.getId())
                        .setHubId(e.getHubId())
                        .setTimestamp(ts)
                        .setTemperatureC(e.getTemperatureC())
                        .setTemperatureF(e.getTemperatureF())
                        .build();
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported sensor event type: " + event.getType());
        }

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }
}