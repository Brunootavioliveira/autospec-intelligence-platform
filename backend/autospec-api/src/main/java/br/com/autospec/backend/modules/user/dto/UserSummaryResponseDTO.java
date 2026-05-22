package br.com.autospec.backend.modules.user.dto;

import br.com.autospec.backend.modules.user.entity.Role;
import br.com.autospec.backend.modules.user.entity.User;


public record UserSummaryResponseDTO(Long id,
                                     String name,
                                     String email,
                                     Role role
) {
    public static UserSummaryResponseDTO from(User user) {
        return new UserSummaryResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}

