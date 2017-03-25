package com.exercise.hotels;

public interface RateLimitHandler {
    public void increase(String apiKey) throws RateLimitExceededException;
}
