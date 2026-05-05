package com.mtslsms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MtsLsmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtsLsmsApplication.class, args);
    }

}
