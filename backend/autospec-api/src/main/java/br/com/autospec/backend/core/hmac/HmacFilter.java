package br.com.autospec.backend.core.hmac;


import br.com.autospec.backend.core.common.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HmacFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${app.security.hmac-secret}")
    private String secret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!requiresHmac(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request);

        String signature = wrappedRequest.getHeader("X-Signature");
        String timestamp = wrappedRequest.getHeader("X-Timestamp");

        if (signature == null || timestamp == null) {
            buildError(response, "Missing HMAC headers");
            return;
        }

        wrappedRequest.getParameterMap();

        byte[] content = wrappedRequest.getContentAsByteArray();
        String body = new String(content, StandardCharsets.UTF_8);

        String expectedSignature =
                HmacUtil.generate(body + timestamp, secret);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        )) {
            buildError(response, "Invalid signature");
            return;
        }

        if (isReplayAttack(timestamp)) {
            buildError(response, "Request expired (possible replay attack)");
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private boolean requiresHmac(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return (path.startsWith("/api/v1/vehicles/spec") && method.equals("POST"))
                || (path.equals("/api/v1/auth/refresh") && method.equals("POST"));
    }

    private boolean isReplayAttack(String timestampHeader) {
        try {
            long requestTime = Long.parseLong(timestampHeader);
            long now = System.currentTimeMillis();

            long diff = Math.abs(now - requestTime);

            return diff > (5 * 60 * 1000);

        } catch (Exception e) {
            return true;
        }
    }

    private void buildError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        var error = new ErrorResponseDTO(
                LocalDateTime.now(),
                401,
                message
        );

        objectMapper.writeValue(response.getWriter(), error);
    }
}