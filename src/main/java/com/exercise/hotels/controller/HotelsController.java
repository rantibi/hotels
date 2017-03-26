package com.exercise.hotels.controller;

import com.exercise.hotels.auth.AuthenticationException;
import com.exercise.hotels.auth.AuthenticationHandler;
import com.exercise.hotels.entity.Hotel;
import com.exercise.hotels.entity.Order;
import com.exercise.hotels.config.HotelsConfig;
import com.exercise.hotels.dal.HotelsDAL;
import com.exercise.hotels.auth.RateLimitExceededException;
import com.exercise.hotels.auth.RateLimitHandler;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/hotels/{api_key}")
public class HotelsController {

    @Inject
    private HotelsConfig config;
    @Inject
    private HotelsDAL hotelsDAL;
    @Inject
    private RateLimitHandler rateLimitHandler;
    @Inject
    private AuthenticationHandler authHandler;


    @RequestMapping("/search")
    public Iterable<Hotel> getHotelsByCityId(@PathVariable(value = "api_key") String apiKey,
                                             @RequestParam(value = "city") String city,
                                             @RequestParam(value = "order", required = false) Order order) throws RateLimitExceededException, AuthenticationException {
        preProcess(apiKey);
        return hotelsDAL.getHotelsByCity(city, order);
    }

    private void preProcess(@PathVariable(value = "api_key") String apiKey) throws AuthenticationException, RateLimitExceededException {
        authHandler.hasPermission(apiKey);
        rateLimitHandler.increase(apiKey);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public void handleRateLimitExceededException(RateLimitExceededException e, HttpServletResponse response) throws IOException {
        response.sendError(429, "Rate limit exceeded, wait until " + Instant.ofEpochMilli(e.getSuspendedUntil()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public void handleIOException(AuthenticationException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
