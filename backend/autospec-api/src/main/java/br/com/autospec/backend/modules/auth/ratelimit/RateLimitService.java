package br.com.autospec.backend.modules.auth.ratelimit;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Service
public class RateLimitService {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public Bucket resolveBucket(String key, RateLimitType type) {
        return cache.get(key, k -> createBucket(type));
    }

    private Bucket createBucket(RateLimitType type) {
        return switch (type) {

            case LOGIN -> Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            5,
                            Refill.greedy(5, Duration.ofMinutes(1))
                    ))
                    .build();

            case SPEC_GENERATION -> Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            10,
                            Refill.greedy(10, Duration.ofMinutes(1))
                    ))
                    .build();

            case REFRESH -> Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            15,
                            Refill.greedy(15, Duration.ofMinutes(1))
                    ))
                    .build();
        };
    }
}
