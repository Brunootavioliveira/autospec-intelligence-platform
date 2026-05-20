package br.com.autospec.backend.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6, max = 100) String newPassword,
        @NotBlank String confirmNewPassword
) {
}
