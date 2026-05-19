package br.com.autospec.backend.modules.vehicle.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record VehicleCompareRequestDTO(
        @NotNull
        @Valid
        VehicleRequestDTO vehicleA,

        @NotNull
        @Valid
        VehicleRequestDTO vehicleB
) {
}
