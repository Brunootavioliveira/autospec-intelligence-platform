package br.com.autospec.backend.core.hmac;


import br.com.autospec.backend.core.common.ErrorResponseDTO;
import br.com.autospec.backend.core.http.CachedBodyRequestWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
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

        CachedBodyRequestWrapper wrappedRequest =
                new CachedBodyRequestWrapper(request);

        String signature = wrappedRequest.getHeader("X-Signature");
        String timestamp = wrappedRequest.getHeader("X-Timestamp");

        if (signature == null || timestamp == null) {
            buildError(response, "Missing HMAC headers");
            return;
        }

        byte[] bodyBytes = wrappedRequest.getCachedBody();

        String rawBody = new String(bodyBytes, StandardCharsets.UTF_8);

        if (rawBody.isBlank()) {
            buildError(response, "Empty request body");
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            buildError(response, "Invalid JSON payload");
            return;
        }

        String normalizedBody = objectMapper.writeValueAsString(jsonNode);

        System.out.println("BODY: " + normalizedBody);
        System.out.println("TIMESTAMP: " + timestamp);

        String data = normalizedBody + timestamp;

        String expectedSignature = HmacUtil.generate(data, secret);

        System.out.println("EXPECTED: " + expectedSignature);
        System.out.println("RECEIVED: " + signature);

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

            return Math.abs(now - requestTime) > (5 * 60 * 1000);

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