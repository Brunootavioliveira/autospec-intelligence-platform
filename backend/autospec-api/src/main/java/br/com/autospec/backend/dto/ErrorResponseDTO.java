package br.com.autospec.backend.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        Integer status,
        String error
) {
}
