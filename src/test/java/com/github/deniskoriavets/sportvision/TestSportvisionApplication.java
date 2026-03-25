package com.github.deniskoriavets.sportvision;

import org.springframework.boot.SpringApplication;

public class TestSportvisionApplication {

    public static void main(String[] args) {
        SpringApplication.from(SportvisionApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
