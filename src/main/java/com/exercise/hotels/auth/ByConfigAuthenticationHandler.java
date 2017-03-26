package com.exercise.hotels.auth;

import com.exercise.hotels.config.HotelsConfig;

import javax.inject.Inject;

public class ByConfigAuthenticationHandler implements AuthenticationHandler{
    @Inject
    private HotelsConfig config;


    @Override
    public void hasPermission(String apiKey) throws AuthenticationException {
        if (!config.getApiKeysRateLimits().keySet().contains(apiKey)){
            throw new AuthenticationException(apiKey);
        }
    }
}
