package br.com.autospec.backend.modules.auth.ratelimit;

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

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )   throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/auth/login")) {

        String ip = request.getRemoteAddr();

        String key = "LOGIN_" + ip;

        Bucket bucket = rateLimitService.resolveBucket(
                key,
                RateLimitType.LOGIN
        );

        if (!bucket.tryConsume(1)) {

            buildTooManyRequestsResponse(response);

            return;
        }
    }

        if (path.startsWith("/api/v1/vehicles/spec")) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (
                authentication != null
                        && authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken)
        ) {

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


private void buildTooManyRequestsResponse(
        HttpServletResponse response
) throws IOException {

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

    response.setContentType("application/json");

    response.getWriter().write("""
            {
              "status": 429,
              "error": "Too Many Requests",
              "message": "Rate limit exceeded"
            }
        """);
}
}
