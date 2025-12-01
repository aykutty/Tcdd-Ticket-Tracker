package com.spring.yhtwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class YhtWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(YhtWatchApplication.class, args);
    }

}
