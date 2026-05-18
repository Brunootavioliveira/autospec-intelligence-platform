package br.com.autospec.backend.modules.auth.service;

import br.com.autospec.backend.core.exception.BusinessException;
import br.com.autospec.backend.modules.auth.entity.RefreshToken;
import br.com.autospec.backend.modules.auth.repository.RefreshTokenRepository;
import br.com.autospec.backend.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        repository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        return repository.save(token);
    }

    @Transactional
    public RefreshToken validate(String token) {
        RefreshToken refresh = repository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (refresh.getExpiryDate().isBefore(Instant.now())) {
            repository.delete(refresh);
            throw new BusinessException("Refresh token expired");
        }

        return refresh;
    }
}
