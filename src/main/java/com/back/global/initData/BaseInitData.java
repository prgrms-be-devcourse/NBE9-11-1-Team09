package com.back.global.initData;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Profile({"default", "local", "dev"})
public class BaseInitData {

    @Bean
    public ApplicationRunner initData() {
        return args -> {
        };
    }
}
