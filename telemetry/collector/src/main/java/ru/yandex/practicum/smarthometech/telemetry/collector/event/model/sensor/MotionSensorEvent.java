package ru.yandex.practicum.smarthometech.telemetry.collector.event.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.smarthometech.telemetry.collector.event.enums.sensor.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends SensorEvent {
    @NotNull
    private int linkQuality;
    @NotNull
    private boolean motion;
    @NotNull
    private int voltage;


    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}
