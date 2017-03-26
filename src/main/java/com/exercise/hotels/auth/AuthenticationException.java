package com.exercise.hotels.auth;

import lombok.Data;

@Data
public class AuthenticationException extends Exception {
   private String apiKey;

    public AuthenticationException(String apiKey) {
        this.apiKey = apiKey;
    }
}
