package br.com.autospec.backend.modules.auth.service;

import br.com.autospec.backend.core.exception.BusinessException;
import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.auth.entity.UserSession;
import br.com.autospec.backend.modules.auth.repository.UserSessionRepository;
import br.com.autospec.backend.modules.user.dto.ActiveSessionResponseDTO;
import br.com.autospec.backend.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository sessionRepository;

    public String createSession(User user, String deviceInfo, String ipAddress, String browserApp) {
        String token = UUID.randomUUID().toString();
        UserSession session = UserSession.builder()
                .user(user)
                .sessionToken(token)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .browserApp(browserApp)
                .createdAt(LocalDateTime.now())
                .lastActive(LocalDateTime.now())
                .active(true)
                .build();
        sessionRepository.save(session);
        return token;
    }

    @Transactional(readOnly = true)
    public List<ActiveSessionResponseDTO> getActiveSessions(User user, String currentSessionToken) {
        return sessionRepository.findByUserAndActiveTrue(user).stream()
                .map(s -> new ActiveSessionResponseDTO(
                        s.getId(),
                        s.getDeviceInfo(),
                        s.getIpAddress(),
                        s.getBrowserApp(),
                        s.getLastActive(),
                        s.getSessionToken().equals(currentSessionToken)
                ))
                .toList();
    }

    @Transactional
    public void revokeSession(Long sessionId, User user) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Session does not belong to user");
        }
        session.setActive(false);
        sessionRepository.save(session);
    }

    @Transactional
    public void revokeAllExcept(User user, String currentToken) {
        sessionRepository.findByUserAndActiveTrue(user).stream()
                .filter(s -> !s.getSessionToken().equals(currentToken))
                .forEach(s -> {
                    s.setActive(false);
                    sessionRepository.save(s);
                });
    }

    @Transactional
    public void updateLastActive(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(s -> {
            s.setLastActive(LocalDateTime.now());
            sessionRepository.save(s);
        });
    }
}
