package br.com.autospec.backend.modules.analysis.dto;

import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;

import java.math.BigDecimal;

public record VehicleAnalysisDTO(
        VehicleResponseDTO vehicle,
        BigDecimal powerToWeightKgKw,
        BigDecimal trackHandlingScore,
        Integer horsepowerPercentile,
        Integer topSpeedPercentile,
        Integer accelerationPercentile
) {
}
