package com.exercise.hotels.auth;

public interface AuthenticationHandler {
    public void hasPermission(String apiKey) throws AuthenticationException;
}
