package br.com.autospec.backend.modules.user.controller;

import br.com.autospec.backend.modules.auth.service.UserSessionService;
import br.com.autospec.backend.modules.user.dto.*;
import br.com.autospec.backend.modules.user.entity.User;
import br.com.autospec.backend.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Gerenciamento de perfil e segurança do usuário")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserSessionService userSessionService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil do usuário autenticado")
    public ResponseEntity<UserProfileResponseDTO> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PatchMapping("/me")
    @Operation(summary = "Atualizar nome do perfil")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Alterar senha")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        userService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/sessions")
    @Operation(summary = "Listar sessões ativas")
    public ResponseEntity<List<ActiveSessionResponseDTO>> getActiveSessions(
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        String currentToken = resolveToken(httpRequest);
        return ResponseEntity.ok(userSessionService.getActiveSessions(user, currentToken));
    }

    @DeleteMapping("/me/sessions/{sessionId}")
    @Operation(summary = "Revogar sessão específica")
    public ResponseEntity<Void> revokeSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId) {
        userSessionService.revokeSession(sessionId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/sessions")
    @Operation(summary = "Revogar todas as outras sessões")
    public ResponseEntity<Void> revokeAllOtherSessions(
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        String currentToken = resolveToken(httpRequest);
        userSessionService.revokeAllExcept(user, currentToken);
        return ResponseEntity.noContent().build();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        return "";
    }
}
