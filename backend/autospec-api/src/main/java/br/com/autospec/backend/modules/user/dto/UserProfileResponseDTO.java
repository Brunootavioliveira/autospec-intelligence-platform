package br.com.autospec.backend.modules.user.dto;

public record UserProfileResponseDTO(
        Long id,
        String name,
        String email,
        String role
) {
}
