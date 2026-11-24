package org.integritylens.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IntegrityLensApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegrityLensApplication.class, args);
    }
}
