package com.zhou.goldtask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GoldTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoldTaskApplication.class, args);
    }

}
