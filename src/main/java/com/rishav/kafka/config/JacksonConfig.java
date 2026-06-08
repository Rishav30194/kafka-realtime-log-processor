package com.rishav.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The plain (non-web) spring-boot-starter does not autoconfigure an ObjectMapper,
 * so we expose one explicitly for the producer and consumers to share.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
