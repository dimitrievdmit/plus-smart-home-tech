package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDto {
    @DecimalMin("1.0")
    private double width;
    @DecimalMin("1.0")
    private double height;
    @DecimalMin("1.0")
    private double depth;
}