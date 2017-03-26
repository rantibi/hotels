package com.exercise.hotels.auth;

public interface RateLimitHandler {
    public void increase(String apiKey) throws RateLimitExceededException;
}
