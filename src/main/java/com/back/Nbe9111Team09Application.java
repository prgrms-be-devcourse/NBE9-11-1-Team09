package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Nbe9111Team09Application {

    public static void main(String[] args) {
        SpringApplication.run(Nbe9111Team09Application.class, args);
    }

}
