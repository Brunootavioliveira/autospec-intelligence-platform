package br.com.autospec.backend.core.security;

import br.com.autospec.backend.config.CorsConfig;
import br.com.autospec.backend.core.hmac.HmacFilter;
import br.com.autospec.backend.modules.auth.filter.JwtFilter;
import br.com.autospec.backend.modules.auth.ratelimit.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class    SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CorsConfig corsConfig;
    private final RateLimitFilter rateLimitFilter;
    private final HmacFilter hmacFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/health-check",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // DELETE só ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**")
                        .hasRole("ADMIN")
                        // POST de spec: ANALYST ou ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/vehicles/**")
                        .hasAnyRole("ANALYST", "ADMIN")
                        // GET: qualquer autenticado (ANALYST, ADMIN, VIEWER)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(hmacFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, HmacFilter.class)
                .addFilterBefore(jwtFilter, RateLimitFilter.class)
                .build();
    }
}
