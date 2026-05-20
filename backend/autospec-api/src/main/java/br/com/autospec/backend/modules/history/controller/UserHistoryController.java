package br.com.autospec.backend.modules.history.controller;

import br.com.autospec.backend.modules.history.dto.UserHistoryResponseDTO;
import br.com.autospec.backend.modules.history.entity.ActionType;
import br.com.autospec.backend.modules.history.service.UserHistoryService;
import br.com.autospec.backend.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "Histórico de análises, comparações e service records do usuário")
@SecurityRequirement(name = "bearerAuth")

public class UserHistoryController {
    private final UserHistoryService historyService;

    @GetMapping
    @Operation(summary = "Listar histórico do usuário (paginado, com filtro por tipo)")
    public ResponseEntity<Page<UserHistoryResponseDTO>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) ActionType actionType,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(historyService.list(user, actionType, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover entrada do histórico (soft delete)")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        historyService.deleteEntry(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Limpar todo o histórico do usuário")
    public ResponseEntity<Void> clearAll(@AuthenticationPrincipal User user) {
        historyService.clearAll(user);
        return ResponseEntity.noContent().build();
    }
}
