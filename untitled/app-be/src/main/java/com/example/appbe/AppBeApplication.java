package com.example.appbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppBeApplication.class, args);
    }

}
