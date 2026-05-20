package br.com.autospec.backend.modules.user.dto;

import java.time.LocalDateTime;

public record ActiveSessionResponseDTO(
        Long id,
        String deviceInfo,
        String ipAddress,
        String browserApp,
        LocalDateTime lastActive,
        boolean currentSession
) {
}
