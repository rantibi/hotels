package com.exercise.hotels.config;

public interface RateLimitConfig {
    long getTimeWindowsExpiredDelay();

    long getRateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount();

    int getRateLimitSuspendedApiKeysLocksCount();

    long getSuspendedCleanThreadTriggerItemsCount();

    CustomRateLimit getRateLimitForAPIKey(String apiKey);
}
