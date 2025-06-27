package com.vulnark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VulnarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulnarkApplication.class, args);
    }

}
