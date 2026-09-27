package br.com.autospec.backend.config;

import br.com.autospec.backend.core.hmac.HmacFilter;
import br.com.autospec.backend.core.security.SecurityConfig;
import br.com.autospec.backend.modules.auth.filter.JwtFilter;
import br.com.autospec.backend.modules.auth.ratelimit.RateLimitFilter;
import br.com.autospec.backend.modules.auth.ratelimit.RateLimitService;
import br.com.autospec.backend.modules.auth.service.JwtService;
import br.com.autospec.backend.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Import({SecurityConfig.class, CorsConfig.class})
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

    @Bean
    @Primary
    public JwtService jwtService() {
        return mock(JwtService.class);
    }

    @Bean
    @Primary
    public AuthenticationProvider authenticationProvider() {
        return mock(DaoAuthenticationProvider.class);
    }

    @Bean
    @Primary
    public JwtFilter jwtFilter(JwtService jwtService, UserRepository userRepository) {
        return new JwtFilter(jwtService, userRepository);
    }

    @Bean
    @Primary
    public RateLimitFilter rateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        return new RateLimitFilter(rateLimitService, objectMapper);
    }

    @Bean
    @Primary
    public HmacFilter hmacFilter(ObjectMapper objectMapper) {
        HmacFilter filter = new HmacFilter(objectMapper);
        ReflectionTestUtils.setField(filter, "secret", "test-secret");
        return filter;
    }
}