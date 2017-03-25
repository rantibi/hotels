package com.exercise.hotels;

import com.exercise.hotels.config.HotelsConfig;
import com.exercise.hotels.dal.csv.CSVHotelsDAL;
import com.exercise.hotels.ratelimit.InMemoryRateLimitHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Configuration
@Import({InMemoryRateLimitHandler.class})
public class ApplicationConfig {

    @Inject
    private Environment environment;

    @Inject
    private HotelsConfig config;

    @Bean
    @Singleton
    public HotelsConfig getConfig() throws IOException {
        return HotelsConfig.load(environment.getProperty("nonOptionArgs"));
    }

    @Bean
    @Singleton
    public CSVHotelsDAL getCSVHotelsDAL() throws IOException {
        return new CSVHotelsDAL(config);
    }
}
