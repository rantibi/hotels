package com.exercise.hotels;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication();
        //application.
        //application.run(args);
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    }
}