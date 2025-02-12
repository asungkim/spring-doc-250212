package com.example.spring_doc.global.init;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
public class DevInitData {

    @Bean
    ApplicationRunner devApplicationRunner() {
        return args -> {
            System.out.println("dev Application runner");
        };
    }
}
