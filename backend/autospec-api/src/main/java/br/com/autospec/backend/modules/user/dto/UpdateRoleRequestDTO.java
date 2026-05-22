package br.com.autospec.backend.modules.user.dto;

import br.com.autospec.backend.modules.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequestDTO(
        @NotNull(message = "Role is required")
        Role role
) {
}
