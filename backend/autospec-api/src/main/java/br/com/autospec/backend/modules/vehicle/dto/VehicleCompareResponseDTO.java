package br.com.autospec.backend.modules.vehicle.dto;

public record VehicleCompareResponseDTO(
        VehicleResponseDTO vehicleA,
        VehicleResponseDTO vehicleB,
        CompareResultDTO comparison
) {
}
