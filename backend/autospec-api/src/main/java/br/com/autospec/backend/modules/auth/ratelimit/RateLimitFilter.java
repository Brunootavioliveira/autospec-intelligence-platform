package br.com.autospec.backend.modules.auth.ratelimit;

import br.com.autospec.backend.core.common.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )   throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.equals("/api/v1/auth/login") && method.equals("POST")) {

            String ip = extractClientIp(request);
            String key = "LOGIN_" + ip;

            Bucket bucket = rateLimitService.resolveBucket(key, RateLimitType.LOGIN);

            if (!bucket.tryConsume(1)) {
                buildTooManyRequestsResponse(response);
                return;
            }
        }

        if (path.equals("/api/v1/auth/refresh") && method.equals("POST")) {

            String ip = extractClientIp(request);
            String key = "REFRESH_" + ip;

            Bucket bucket = rateLimitService.resolveBucket(key, RateLimitType.REFRESH);

            if (!bucket.tryConsume(1)) {
                buildTooManyRequestsResponse(response);
                return;
            }
        }

        if (path.startsWith("/api/v1/vehicles/spec")) {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {

                String username = authentication.getName();
                String key = "SPEC_" + username;

                Bucket bucket = rateLimitService.resolveBucket(
                        key,
                        RateLimitType.SPEC_GENERATION
                );

                if (!bucket.tryConsume(1)) {
                    buildTooManyRequestsResponse(response);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0];
        }

        return request.getRemoteAddr();
    }

    private void buildTooManyRequestsResponse(HttpServletResponse response) throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");

        var error = new ErrorResponseDTO(
                LocalDateTime.now(),
                429,
                "Too Many Requests"
        );

        objectMapper.writeValue(response.getWriter(), error);
    }
}
