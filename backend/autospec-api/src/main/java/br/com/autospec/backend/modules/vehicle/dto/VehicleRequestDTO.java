package br.com.autospec.backend.modules.vehicle.dto;

import jakarta.validation.constraints.*;

public record VehicleRequestDTO(

        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand must have a maximum of 100 characters")
        String brand,

        @NotBlank(message = "Model is required")
        @Size(max = 100, message = "Model must have a maximum of 100 characters")
        String model,

        @NotBlank(message = "Version is required")
        @Size(max = 100, message = "Version must have a maximum of 100 characters")
        String version,

        @Min(value = 1900, message = "Year must be greater than or equal to 1900")
        @Max(value = 2030, message = "Year must be less than or equal to 2030")
        @NotNull(message = "Year is required")
        Integer year
) {
}
