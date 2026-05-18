package br.com.autospec.backend.modules.auth.dto;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String name,
        String role
) {
}
