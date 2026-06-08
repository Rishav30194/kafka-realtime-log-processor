package com.rishav.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishav.kafka.config.KafkaTopics;
import com.rishav.kafka.model.LogMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogRouter {

    private static final Logger log = LoggerFactory.getLogger(LogRouter.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = KafkaTopics.LOGS, groupId = "log-router")
    public void route(String message) throws JsonProcessingException {
        LogMessage logMessage = objectMapper.readValue(message, LogMessage.class);
        if ("ERROR".equalsIgnoreCase(logMessage.getLevel())) {
            kafkaTemplate.send(KafkaTopics.ERROR_LOGS, logMessage.getLevel(), message);
            log.debug("Routed error log to {}", KafkaTopics.ERROR_LOGS);
        }
    }
}
