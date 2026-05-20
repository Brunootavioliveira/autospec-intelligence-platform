package br.com.autospec.backend.modules.report.dto;

import jakarta.validation.constraints.NotNull;

public record VehicleDossierRequestDTO(
        @NotNull Long vehicleId
) {
}
