package io.home.staging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HomeStagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeStagingApplication.class, args);
    }

}
