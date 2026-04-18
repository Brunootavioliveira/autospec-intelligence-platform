package br.com.autospec.backend.dto;

public record VehicleResponseDTO(
        String engine,
        int horsepower,
        int torque,
        String drivetrain,
        int price
) {
}
