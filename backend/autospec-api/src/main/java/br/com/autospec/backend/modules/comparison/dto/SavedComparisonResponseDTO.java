package br.com.autospec.backend.modules.comparison.dto;

import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;
import java.time.LocalDateTime;

public record SavedComparisonResponseDTO(
        Long id,
        String title,
        VehicleResponseDTO vehicleA,
        VehicleResponseDTO vehicleB,
        LocalDateTime savedAt
) {
}
