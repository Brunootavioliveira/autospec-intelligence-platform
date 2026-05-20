package br.com.autospec.backend.modules.report.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record ComparisonReportRequestDTO(
        @NotNull Long vehicleAId,
        @NotNull Long vehicleBId,
        Set<String> parameters
) {
}
