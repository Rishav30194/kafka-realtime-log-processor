package com.rishav.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishav.kafka.model.LogMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class LogProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TOPIC = "logs";

    @Async
    @PostConstruct
    public void produce() throws JsonProcessingException, InterruptedException {
        String[] levels = {"INFO", "DEBUG", "ERROR"};
        Random random = new Random();
        for(int i = 0; i < 50; i++) {
            String level = levels[random.nextInt(levels.length)];
            LogMessage log = new LogMessage(level, System.currentTimeMillis(), "This is a " + level +
                    "message #" + i);
            String logJson = objectMapper.writeValueAsString(log);

            kafkaTemplate.send(TOPIC, level, logJson);
            System.out.println("Sent: " + logJson);
            Thread.sleep(200);
        }
    }

}
