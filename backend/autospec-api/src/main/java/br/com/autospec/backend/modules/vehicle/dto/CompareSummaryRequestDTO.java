package br.com.autospec.backend.modules.vehicle.dto;

public record CompareSummaryRequestDTO(
        VehicleResponseDTO vehicleA,
        VehicleResponseDTO vehicleB,
        String winner
) {
}
