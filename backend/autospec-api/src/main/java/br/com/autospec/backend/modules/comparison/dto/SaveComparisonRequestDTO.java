package br.com.autospec.backend.modules.comparison.dto;

import jakarta.validation.constraints.NotNull;

public record SaveComparisonRequestDTO(
        @NotNull Long vehicleAId,
        @NotNull Long vehicleBId,
        String title
) {
}
