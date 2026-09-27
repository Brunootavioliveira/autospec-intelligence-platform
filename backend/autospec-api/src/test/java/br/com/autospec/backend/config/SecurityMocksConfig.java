package br.com.autospec.backend.config;

import br.com.autospec.backend.modules.auth.ratelimit.RateLimitService;
import br.com.autospec.backend.modules.user.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class SecurityMocksConfig {

    @Bean
    @Primary
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }

    @Bean
    @Primary
    public RateLimitService rateLimitService() {
        return new RateLimitService();
    }
}