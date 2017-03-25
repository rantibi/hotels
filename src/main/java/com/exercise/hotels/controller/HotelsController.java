package com.exercise.hotels.controller;

import com.exercise.hotels.entity.Hotel;
import com.exercise.hotels.entity.Order;
import com.exercise.hotels.config.HotelsConfig;
import com.exercise.hotels.dal.HotelsDAL;
import com.exercise.hotels.ratelimit.RateLimitExceededException;
import com.exercise.hotels.ratelimit.RateLimitHandler;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/hotels/{apiKey}")
public class HotelsController {

    @Inject
    private HotelsConfig config;
    @Inject
    private HotelsDAL hotelsDAL;
    @Inject
    private RateLimitHandler rateLimitHandler;

    @RequestMapping("/search")
    public Iterable<Hotel> getHotelsByCityId(@PathVariable String apiKey,
                                             @RequestParam(value = "city") String city,
                                             @RequestParam(value = "order", required = false) Order order) throws RateLimitExceededException {
        rateLimitHandler.increase(apiKey);
        return hotelsDAL.getHotelsByCity(city, order);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public void handleIOException(RateLimitExceededException e, HttpServletResponse response) throws IOException {
        response.sendError(429, "Rate limit exceeded, wait until " + Instant.ofEpochMilli(e.getSuspendedUntil()));
    }
}
