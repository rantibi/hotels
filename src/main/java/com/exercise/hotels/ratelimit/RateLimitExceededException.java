package com.exercise.hotels.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RateLimitExceededException extends Exception {
    @Getter
    private long suspendedUntil;
}
