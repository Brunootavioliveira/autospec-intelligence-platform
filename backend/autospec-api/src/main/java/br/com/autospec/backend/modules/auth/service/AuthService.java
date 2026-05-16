package br.com.autospec.backend.modules.auth.service;

import br.com.autospec.backend.modules.auth.dto.AuthResponseDTO;
import br.com.autospec.backend.modules.auth.dto.LoginRequestDTO;
import br.com.autospec.backend.modules.auth.dto.RegisterRequestDTO;
import br.com.autospec.backend.modules.user.entity.User;
import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.user.repository.UserRepository;
import br.com.autospec.backend.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;


    public AuthResponseDTO register(RegisterRequestDTO request) {

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userService.create(user);
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, user.getName(), user.getRole().name());
    }


    public AuthResponseDTO login(LoginRequestDTO request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userService.findByEmail(request.email());
        
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, user.getName(), user.getRole().name());
    }
}

