package com.exercise.hotels;

import lombok.EqualsAndHashCode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryRateLimitHandler implements RateLimitHandler {

    private RateLimitConfig config;
    private ConcurrentHashMap<APIKeyTimeWindow, AtomicLong> requestForAPIKey = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> suspendedAPIKeys = new ConcurrentHashMap<>();
    private AtomicBoolean cleanThreadActive = new AtomicBoolean(false);

    public InMemoryRateLimitHandler(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public void increase(String apiKey) throws RateLimitExceededException {
        long currMillis = System.currentTimeMillis();
        checkSuspended(apiKey, currMillis);

        AtomicLong newRequests = new AtomicLong(0);
        AtomicLong requests = requestForAPIKey.putIfAbsent(new APIKeyTimeWindow(apiKey, currMillis), newRequests);

        if (requests == null) {
            requests = newRequests;
            cleanExpiredAPIKeyTimeWindows();
        }

        long currentRequests = requests.incrementAndGet();
        suspendIfRateLimitExceeded(apiKey, currMillis, currentRequests);
    }

    private void suspendIfRateLimitExceeded(String apiKey, long currMillis, long currentRequests) throws RateLimitExceededException {
        if (currentRequests > config.getRateLimitForAPIKey(apiKey).getRequestsRateLimit()) {
            suspendedAPIKeys.putIfAbsent(apiKey,
                    currMillis + TimeUnit.SECONDS.toMillis(config.getRateLimitForAPIKey(apiKey).getRateLimitSuspendSeconds()));
            throw new RateLimitExceededException(suspendedAPIKeys.get(apiKey));
        }
    }

    private void checkSuspended(String apiKey, long currMillis) throws RateLimitExceededException {
        Long suspendedUntil = suspendedAPIKeys.get(apiKey);

        if (suspendedUntil == null) {
            return;
        }

        // TODO: check again if there is race issue here
        if (currMillis > suspendedUntil) {
            throw new RateLimitExceededException(suspendedAPIKeys.get(apiKey));
        } else {
            suspendedAPIKeys.remove(apiKey);
        }
    }

    private void cleanExpiredAPIKeyTimeWindows() {
        if (requestForAPIKey.size() >= config.getRateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount()) {
            if (!cleanThreadActive.getAndSet(true)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestForAPIKey.keySet().stream()
                                    .filter(apiKeyTimeWindow -> apiKeyTimeWindow.isExpired(System.currentTimeMillis()))
                                    .forEach(apiKeyTimeWindow -> requestForAPIKey.remove(apiKeyTimeWindow));
                        } finally {
                            cleanThreadActive.set(false);
                        }
                    }
                }).start();
            }
        }
    }

    @EqualsAndHashCode
    class APIKeyTimeWindow {
        private String apiKey;
        private long timeWindow;

        public APIKeyTimeWindow(String apiKey, long currMillis) {
            this.apiKey = apiKey;
            this.timeWindow = calcTimeWindow(currMillis);
        }

        public boolean isExpired(long currMillis) {
            long currTimeWindow = calcTimeWindow(currMillis);
            return (currTimeWindow > timeWindow + config.getTimeWindowsExpiredDelay());
        }

        private long calcTimeWindow(long currMillis) {
            return currMillis / TimeUnit.SECONDS.toMillis(config.getRateLimitForAPIKey(apiKey).getRateLimitTimeWindowSeconds());
        }
    }

}
