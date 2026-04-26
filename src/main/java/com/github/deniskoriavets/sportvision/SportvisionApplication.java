package com.github.deniskoriavets.sportvision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SportvisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportvisionApplication.class, args);
    }

}
