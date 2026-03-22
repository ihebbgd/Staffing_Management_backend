package com.demo.staffing_management_backend.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L;

    private record Attempt(int count, long firstAttemptAt) {
    }

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }
        if (System.currentTimeMillis() - attempt.firstAttemptAt() > WINDOW_MS) {
            attempts.remove(key);
            return false;
        }
        return attempt.count() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        long now = System.currentTimeMillis();
        attempts.compute(key, (k, existing) -> {
            if (existing == null || now - existing.firstAttemptAt() > WINDOW_MS) {
                return new Attempt(1, now);
            }
            return new Attempt(existing.count() + 1, existing.firstAttemptAt());
        });
    }

    public void reset(String key) {
        attempts.remove(key);
    }
}
