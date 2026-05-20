package br.com.autospec.backend.modules.garage.dto;

import br.com.autospec.backend.modules.garage.entity.FleetType;
import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;

import java.time.LocalDateTime;

public record GarageVehicleResponseDTO(
        Long id,
        VehicleResponseDTO vehicleSpec,
        FleetType fleetType,
        String nickname,
        boolean active,
        LocalDateTime addedAt
) {
}
