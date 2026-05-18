package br.com.autospec.backend.modules.auth.controller;

import br.com.autospec.backend.modules.auth.dto.AuthResponseDTO;
import br.com.autospec.backend.modules.auth.dto.LoginRequestDTO;
import br.com.autospec.backend.modules.auth.dto.RefreshRequestDTO;
import br.com.autospec.backend.modules.auth.dto.RegisterRequestDTO;
import br.com.autospec.backend.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Roles disponíveis: ANALYST, ADMIN, VIEWER")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }


    @PostMapping("/login")
    @Operation(summary = "Login", description = "Retorna token JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponseDTO> refresh(
            @Valid @RequestBody RefreshRequestDTO request) {

        return ResponseEntity.ok(
                authService.refresh(request.refreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDTO request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}

