package com.rishav.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishav.kafka.config.KafkaTopics;
import com.rishav.kafka.model.LogMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class LogProducer {

    private static final Logger log = LoggerFactory.getLogger(LogProducer.class);
    private static final String[] LEVELS = {"INFO", "DEBUG", "ERROR"};

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.producer.enabled:true}")
    private boolean enabled;

    @Value("${app.producer.count:50}")
    private int count;

    @Value("${app.producer.delay-ms:200}")
    private long delayMs;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void produceOnStartup() throws JsonProcessingException, InterruptedException {
        if (!enabled) {
            return;
        }
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            String level = LEVELS[random.nextInt(LEVELS.length)];
            LogMessage message = new LogMessage(level, System.currentTimeMillis(),
                    "This is a " + level + " message #" + i);
            send(message);
            Thread.sleep(delayMs);
        }
    }

    public void send(LogMessage message) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(message);
        kafkaTemplate.send(KafkaTopics.LOGS, message.getLevel(), json);
        log.info("Produced: {}", json);
    }
}
