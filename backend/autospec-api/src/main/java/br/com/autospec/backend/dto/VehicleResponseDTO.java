package br.com.autospec.backend.dto;

import java.math.BigDecimal;

public record VehicleResponseDTO(
        String engine,
        Integer horsepower,
        BigDecimal torque,
        String drivetrain,
        Integer topSpeed,
        BigDecimal acceleration,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        BigDecimal weight,
        Integer electricRange,
        BigDecimal price
) {
}
