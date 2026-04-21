package br.com.autospec.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequestDTO(

        @NotBlank(message = "Brand is required")
        String brand,

        @NotBlank(message = "Model is required")
        String model,

        @NotBlank(message = "Version is required")
        String version,

        @NotNull(message = "Year is required")
        Integer year
) {
}
