package com.exercise.hotels.ratelimit;

public interface RateLimitHandler {
    public void increase(String apiKey) throws RateLimitExceededException;
}
