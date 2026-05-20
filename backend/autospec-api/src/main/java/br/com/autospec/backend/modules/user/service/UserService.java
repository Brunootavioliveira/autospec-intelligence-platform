package br.com.autospec.backend.modules.user.service;

import br.com.autospec.backend.core.exception.BusinessException;
import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.user.dto.ChangePasswordRequestDTO;
import br.com.autospec.backend.modules.user.dto.UpdateProfileRequestDTO;
import br.com.autospec.backend.modules.user.dto.UserProfileResponseDTO;
import br.com.autospec.backend.modules.user.entity.User;
import br.com.autospec.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BusinessException("Email já cadastrado");
        }
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o email: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public UserProfileResponseDTO getProfile(User user) {
        return new UserProfileResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    @Transactional
    public UserProfileResponseDTO updateProfile(User user, UpdateProfileRequestDTO request) {
        user.updateName(request.name());
        userRepository.save(user);
        return new UserProfileResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequestDTO request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta");
        }
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BusinessException("As senhas não coincidem");
        }
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
