package br.com.autospec.backend.modules.auth.dto;

public record AuthResponseDTO(
        String token,
        String name,
        String role
) {
}
