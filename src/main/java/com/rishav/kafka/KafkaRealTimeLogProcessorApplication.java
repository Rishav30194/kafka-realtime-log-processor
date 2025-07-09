package com.rishav.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KafkaRealTimeLogProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaRealTimeLogProcessorApplication.class, args);
    }
}
