package com.rrhh.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RrhhSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(RrhhSaasApplication.class, args);
    }
}
