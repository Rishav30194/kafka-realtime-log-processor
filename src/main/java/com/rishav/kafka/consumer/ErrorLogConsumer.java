package com.rishav.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishav.kafka.model.LogMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ErrorLogConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "logs", groupId = "error-log-group-dev")
    public void consume(String message) throws JsonProcessingException {
        LogMessage log = objectMapper.readValue(message, LogMessage.class);
        if("ERROR".equalsIgnoreCase(log.getLevel())){
            System.out.println("Error Log:" + log);
        }
    }

}
