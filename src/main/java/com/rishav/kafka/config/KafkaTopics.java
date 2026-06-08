package com.rishav.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopics {

    public static final String LOGS = "logs";
    public static final String ERROR_LOGS = "error-logs";

    @Bean
    public NewTopic logsTopic() {
        return TopicBuilder.name(LOGS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic errorLogsTopic() {
        return TopicBuilder.name(ERROR_LOGS).partitions(1).replicas(1).build();
    }
}
