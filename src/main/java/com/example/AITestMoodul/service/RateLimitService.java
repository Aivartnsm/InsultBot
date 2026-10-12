package com.example.AITestMoodul.service;

import com.example.AITestMoodul.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lihtne mälupõhine rate limiter (sliding window): iga klient (IP) võib teha
 * maxRequests päringut ajaakna jooksul. Iga /api/chat päring maksab kuni 3 AI-kutset,
 * seega kaitseb see Gemini kvooti spämmimise eest.
 * <p>
 * Andmed on ainult selle rakenduse mälus - mitme serveri puhul oleks vaja jagatud salvestust (nt Redis).
 */
@Slf4j
@Service
public class RateLimitService {

    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final int maxRequests;
    private final Duration window;
    private final Map<String, Deque<Instant>> requestsByClient = new ConcurrentHashMap<>();

    public RateLimitService(@Value("${insultbot.rate-limit.max-requests:5}") int maxRequests,
                            @Value("${insultbot.rate-limit.window-seconds:60}") long windowSeconds) {
        this.maxRequests = maxRequests;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    public void checkLimit(String clientKey) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(window);
        Instant[] oldestBlocking = {null};

        // compute() on ühe võtme jaoks atomaarne, nii et samaaegsed päringud ei loe valesti
        requestsByClient.compute(clientKey, (key, timestamps) -> {
            Deque<Instant> recent = timestamps == null ? new ArrayDeque<>() : timestamps;
            removeExpired(recent, windowStart);
            if (recent.size() < maxRequests) {
                recent.addLast(now);
            } else {
                oldestBlocking[0] = recent.peekFirst();
            }
            return recent;
        });

        if (requestsByClient.size() > MAX_TRACKED_CLIENTS) {
            removeInactiveClients(windowStart);
        }

        if (oldestBlocking[0] != null) {
            // Uus sõnum lubatakse, kui vanim aknas olev päring aegub
            long retryAfterSeconds = Math.max(1, Duration.between(now, oldestBlocking[0].plus(window)).toSeconds() + 1);
            log.warn("[RateLimit] blocked client={} (max {} per {}s), retry in {}s",
                    clientKey, maxRequests, window.toSeconds(), retryAfterSeconds);
            throw new RateLimitExceededException(retryAfterSeconds);
        }
    }

    // Kustutab kliendid, kellel pole ajaaknas ühtegi päringut, et map lõputult ei kasvaks
    private void removeInactiveClients(Instant windowStart) {
        requestsByClient.keySet().forEach(key -> requestsByClient.computeIfPresent(key, (k, timestamps) -> {
            removeExpired(timestamps, windowStart);
            return timestamps.isEmpty() ? null : timestamps;
        }));
    }

    private static void removeExpired(Deque<Instant> timestamps, Instant windowStart) {
        while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(windowStart)) {
            timestamps.pollFirst();
        }
    }
}
