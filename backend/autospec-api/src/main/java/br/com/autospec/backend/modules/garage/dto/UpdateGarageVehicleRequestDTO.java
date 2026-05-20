package br.com.autospec.backend.modules.garage.dto;

import br.com.autospec.backend.modules.garage.entity.FleetType;

public record UpdateGarageVehicleRequestDTO(
        FleetType fleetType,
        String nickname
) {
}
