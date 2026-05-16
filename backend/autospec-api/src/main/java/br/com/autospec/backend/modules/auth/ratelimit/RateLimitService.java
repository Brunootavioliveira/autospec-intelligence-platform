package br.com.autospec.backend.modules.auth.ratelimit;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, RateLimitType type) {

        return cache.computeIfAbsent(
                key,
                k -> createBucket(type)
        );
    }

    private Bucket createBucket(RateLimitType type) {

        return switch (type) {

            case LOGIN -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    5,
                                    Refill.greedy(5, Duration.ofMinutes(1))
                            )
                    )
                    .build();

            case SPEC_GENERATION -> Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    10,
                                    Refill.greedy(10, Duration.ofMinutes(1))
                            )
                    )
                    .build();
        };
    }
}
