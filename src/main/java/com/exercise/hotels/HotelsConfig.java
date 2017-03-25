package com.exercise.hotels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Data
public class HotelsConfig implements RateLimitConfig {
    @JsonProperty("rate_limit_suspend_seconds")
    private long rateLimitSuspendSeconds = 30;
    @JsonProperty("rate_limit_time_window_seconds")
    private long rateLimitTimeWindowSeconds = 30;
    @JsonProperty("requests_rate_limit")
    private long requestsRateLimit = 3;
    @JsonProperty("rate_limit_for_api_key_time_window_map_clean_thread_trigger_on_items_count")
    private long rateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount;
    @JsonProperty("time_windows_expired_delay")
    private long timeWindowsExpiredDelay = 2;

    @JsonProperty("api_keys_custom_rate_limits")
    Map<String, CustomRateLimit> apiKeysCustomRateLimits = new HashMap<>();

    @Getter(lazy = true)
    private final CustomRateLimit defaultRateLimits = createDefaultRateLimits();

    private CustomRateLimit createDefaultRateLimits() {
        return CustomRateLimit.builder()
                .rateLimitSuspendSeconds(rateLimitSuspendSeconds)
                .rateLimitTimeWindowSeconds(rateLimitTimeWindowSeconds)
                .requestsRateLimit(requestsRateLimit)
                .build();
    }

    @Override
    public CustomRateLimit getRateLimitForAPIKey(String apiKey) {
        return apiKeysCustomRateLimits.getOrDefault(apiKey, getDefaultRateLimits());
    }
}
