package com.exercise.hotels.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LongSummaryStatistics;

@Data
@NoArgsConstructor
public class RateLimit {
    public static final long DEFAULT_RATE_LIMIT_SUSPEND_SECONDS = 300;
    public static final long DEFAULT_RATE_LIMIT_TIME_WINDOW_SECONDS = 10;
    public static final long DEFAULT_REQUESTS_RATE_LIMIT = 1;

    @JsonProperty("rate_limit_suspend_seconds")
    private Long rateLimitSuspendSeconds;
    @JsonProperty("rate_limit_time_window_seconds")
    private Long rateLimitTimeWindowSeconds;
    @JsonProperty("requests_rate_limit")
    private Long requestsRateLimit;

    public void updateNullValues(RateLimit defaultRateLimit) {
        if (rateLimitSuspendSeconds == null) {
            rateLimitSuspendSeconds = defaultRateLimit.rateLimitSuspendSeconds;
        }

        if (rateLimitTimeWindowSeconds == null) {
            rateLimitTimeWindowSeconds = defaultRateLimit.rateLimitTimeWindowSeconds;
        }

        if (requestsRateLimit == null) {
            requestsRateLimit = defaultRateLimit.requestsRateLimit;
        }
    }

    @JsonIgnore
    public static RateLimit getDefault() {
        RateLimit rateLimit = new RateLimit();
        rateLimit.rateLimitSuspendSeconds = RateLimit.DEFAULT_RATE_LIMIT_SUSPEND_SECONDS;
        rateLimit.rateLimitTimeWindowSeconds = RateLimit.DEFAULT_RATE_LIMIT_TIME_WINDOW_SECONDS;
        rateLimit.requestsRateLimit = RateLimit.DEFAULT_REQUESTS_RATE_LIMIT;
        return rateLimit;
    }
}
