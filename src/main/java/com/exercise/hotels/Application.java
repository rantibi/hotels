package com.exercise.hotels;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.logging.Level;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Exactly one argument should be path to the configuration file");
            System.exit(1);
        }

        SpringApplication.run(Application.class, args);
    }
}