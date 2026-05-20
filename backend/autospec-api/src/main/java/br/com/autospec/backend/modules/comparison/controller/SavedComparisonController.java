package br.com.autospec.backend.modules.comparison.controller;

import br.com.autospec.backend.modules.comparison.dto.SaveComparisonRequestDTO;
import br.com.autospec.backend.modules.comparison.dto.SavedComparisonResponseDTO;
import br.com.autospec.backend.modules.comparison.service.SavedComparisonService;
import br.com.autospec.backend.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comparisons/saved")
@RequiredArgsConstructor
@Tag(name = "Saved Comparisons", description = "Comparações salvas pelo usuário")
@SecurityRequirement(name = "bearerAuth")
public class SavedComparisonController {
    private final SavedComparisonService savedComparisonService;

    @PostMapping
    @Operation(summary = "Salvar comparação")
    public ResponseEntity<SavedComparisonResponseDTO> save(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SaveComparisonRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComparisonService.save(user, request));
    }

    @GetMapping
    @Operation(summary = "Listar comparações salvas (paginado)")
    public ResponseEntity<Page<SavedComparisonResponseDTO>> list(
            @AuthenticationPrincipal User user,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(savedComparisonService.list(user, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover comparação salva")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        savedComparisonService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
