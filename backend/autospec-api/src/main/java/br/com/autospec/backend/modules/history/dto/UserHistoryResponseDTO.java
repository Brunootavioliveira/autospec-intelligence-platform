package br.com.autospec.backend.modules.history.dto;

import br.com.autospec.backend.modules.history.entity.ActionType;
import java.time.LocalDateTime;

public record UserHistoryResponseDTO (
        Long id,
        ActionType actionType,
        String title,
        String description,
        Long referenceId,
        LocalDateTime createdAt
){
}
