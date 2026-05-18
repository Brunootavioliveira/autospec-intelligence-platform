package br.com.autospec.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank String refreshToken
) {
}
