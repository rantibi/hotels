package com.exercise.hotels.auth;

import com.exercise.hotels.clock.Clock;
import com.exercise.hotels.config.RateLimitConfig;
import com.google.common.util.concurrent.Striped;
import lombok.EqualsAndHashCode;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

@Singleton
public class InMemoryRateLimitHandler implements RateLimitHandler {
    @Inject
    private RateLimitConfig config;

    @Inject
    private Clock clock;

    private ConcurrentHashMap<APIKeyTimeWindow, AtomicLong> requestForAPIKey = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> suspendedAPIKeys = new ConcurrentHashMap<>();
    private AtomicBoolean isCleanRequestForAPIKeyThreadActive = new AtomicBoolean(false);
    private AtomicBoolean isCleanSuspendedAPIKeysThreadActive = new AtomicBoolean(false);
    private Striped<Lock> locks;

    public InMemoryRateLimitHandler(RateLimitConfig config, Clock clock) {
        this.config = config;
        this.clock = clock;
        locks = Striped.lock(config.getRateLimitSuspendedApiKeysLocksCount());
    }

    @Override
    public void increase(String apiKey) throws RateLimitExceededException {
        long currMillis = clock.currentTimeMillis();
        checkSuspended(apiKey, currMillis);

        AtomicLong newRequests = new AtomicLong(0);
        AtomicLong requests = requestForAPIKey.putIfAbsent(new APIKeyTimeWindow(apiKey, currMillis), newRequests);

        if (requests == null) {
            requests = newRequests;
            cleanExpiredAPIKeyTimeWindows(currMillis);
        }

        long currentRequests = requests.incrementAndGet();
        suspendIfRateLimitExceeded(apiKey, currMillis, currentRequests);
    }

    private void suspendIfRateLimitExceeded(String apiKey, long currMillis, long currentRequests) throws RateLimitExceededException {
        if (currentRequests > config.getRateLimitForAPIKey(apiKey).getRequestsRateLimit()) {
            Long suspendUntil;
            Lock lock = locks.get(apiKey);
            try {
                lock.lock();
                long newSuspendUntil = currMillis + TimeUnit.SECONDS.toMillis(config.getRateLimitForAPIKey(apiKey).getRateLimitSuspendSeconds());
                suspendUntil = suspendedAPIKeys.putIfAbsent(apiKey, newSuspendUntil);
                if (suspendUntil == null) {
                    suspendUntil = newSuspendUntil;
                }
            } finally {
                lock.unlock();
            }

            cleanExpiredSuspendedAPIKeys(currMillis);
            throw new RateLimitExceededException(suspendUntil);
        }
    }

    private void checkSuspended(String apiKey, long currMillis) throws RateLimitExceededException {
        Long suspendedUntil = suspendedAPIKeys.get(apiKey);

        if (suspendedUntil == null) {
            return;
        }

        if (currMillis < suspendedUntil) {
            throw new RateLimitExceededException(suspendedAPIKeys.get(apiKey));
        } else {
            Lock lock = locks.get(apiKey);

            try {
                lock.lock();
                suspendedAPIKeys.remove(apiKey);
            } finally {
                lock.unlock();
            }
        }
    }

    private void cleanExpiredAPIKeyTimeWindows(long currMillis) {
        if (requestForAPIKey.size() >= config.getRateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount()) {
            if (!isCleanRequestForAPIKeyThreadActive.getAndSet(true)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestForAPIKey.keySet().stream()
                                    .filter(apiKeyTimeWindow -> apiKeyTimeWindow.isExpired(currMillis))
                                    .forEach(apiKeyTimeWindow -> requestForAPIKey.remove(apiKeyTimeWindow));
                        } finally {
                            isCleanRequestForAPIKeyThreadActive.set(false);
                        }
                    }
                }).start();
            }
        }
    }

    private void cleanExpiredSuspendedAPIKeys(long currMillis) {
        if (suspendedAPIKeys.size() >= config.getSuspendedCleanThreadTriggerItemsCount()) {
            if (!isCleanSuspendedAPIKeysThreadActive.getAndSet(true)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            suspendedAPIKeys.entrySet().stream()
                                    .filter(entry -> {
                                        return entry.getValue() < currMillis;
                                    })
                                    .forEach(entry -> {
                                        Lock lock = locks.get(entry.getKey());

                                        try {
                                            lock.lock();
                                            suspendedAPIKeys.remove(entry.getKey());
                                        } finally {
                                            lock.unlock();
                                        }
                                    });
                        } finally {
                            isCleanSuspendedAPIKeysThreadActive.set(false);
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
