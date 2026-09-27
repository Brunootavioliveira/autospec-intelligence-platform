package br.com.autospec.backend.modules.auth.controller;

import br.com.autospec.backend.config.SecurityMocksConfig;
import br.com.autospec.backend.modules.auth.dto.AuthResponseDTO;
import br.com.autospec.backend.modules.auth.dto.LoginRequestDTO;
import br.com.autospec.backend.modules.auth.dto.RegisterRequestDTO;
import br.com.autospec.backend.modules.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityMocksConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_comCredenciaisValidas_deveRetornar200EToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@teste.com", "senha123");
        AuthResponseDTO response = new AuthResponseDTO("access-token", "refresh-token", "User Teste", "ANALYST");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.role").value("ANALYST"));
    }

    @Test
    void register_comDadosValidos_deveRetornar201() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("Novo User", "novo@teste.com", "senha12345");
        AuthResponseDTO response = new AuthResponseDTO("access-token", "refresh-token", "Novo User", "VIEWER");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    void login_comEmailInvalido_deveRetornar400() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("email-invalido", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_comSenhaCurta_deveRetornar400() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("User", "user@teste.com", "123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_comCredenciaisErradas_deveRetornar401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@teste.com", "senhaErrada");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}