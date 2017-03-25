package com.exercise.hotels;

import java.util.Map;

public interface RateLimitConfig {
    long getTimeWindowsExpiredDelay();
    long getRateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount();
    CustomRateLimit getRateLimitForAPIKey(String apiKey);
}
