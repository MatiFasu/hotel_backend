package com.hackacode.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Bean
    public Clock clock() {
        return Clock.system(DEFAULT_ZONE);
    }
}
