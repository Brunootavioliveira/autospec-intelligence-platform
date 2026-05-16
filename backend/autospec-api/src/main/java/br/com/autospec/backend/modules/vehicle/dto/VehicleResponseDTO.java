package br.com.autospec.backend.modules.vehicle.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record VehicleResponseDTO (
        Long id,
        String brand,
        String model,
        String version,
        Integer year,
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
) implements Serializable {
}
