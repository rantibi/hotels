package com.exercise.hotels;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController()
@RequestMapping("/hotels/{apiKey}")
public class HotelsController {

    private HotelsConfig config;
    private HotelsDAL hotelsDAL;
    private RateLimitHandler rateLimitHandler;


    public HotelsController() throws IOException {
        this.config = new HotelsConfig();
        this.hotelsDAL = new CSVHotelsDAL("/Users/rantibi/hotels_exercise/src/test/resources/hoteldb.csv");
        this.rateLimitHandler = new InMemoryRateLimitHandler(config);
    }

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
