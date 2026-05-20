package br.com.autospec.backend.modules.garage.dto;

import br.com.autospec.backend.modules.garage.entity.FleetType;
import jakarta.validation.constraints.NotNull;

public record AddVehicleToGarageRequestDTO(
        @NotNull Long vehicleSpecId,
        @NotNull FleetType fleetType,
        String nickname
) {
}
