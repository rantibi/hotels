package com.exercise.hotels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CustomRateLimit {
    @JsonProperty("rate_limit_suspend_seconds")
    private long rateLimitSuspendSeconds = 300;
    @JsonProperty("rate_limit_time_window_seconds")
    private long rateLimitTimeWindowSeconds = 10;
    @JsonProperty("requests_rate_limit")
    private long requestsRateLimit = 1;
}
