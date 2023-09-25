package com.amikhalchenko.hrselen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class HrselenApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrselenApplication.class, args);
    }

}
