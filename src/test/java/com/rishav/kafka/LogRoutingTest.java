package com.rishav.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishav.kafka.config.KafkaTopics;
import com.rishav.kafka.model.LogMessage;
import com.rishav.kafka.producer.LogProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.producer.enabled=false",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.LOGS, KafkaTopics.ERROR_LOGS})
class LogRoutingTest {

    @Autowired
    private LogProducer producer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    private Consumer<String, String> errorLogConsumer;

    @BeforeEach
    void subscribe() {
        // a fresh group per test so offsets never carry over between tests
        errorLogConsumer = consumerFactory.createConsumer("test-" + UUID.randomUUID(), "");
        embeddedKafka.consumeFromAnEmbeddedTopic(errorLogConsumer, KafkaTopics.ERROR_LOGS);
        // ignore anything already on the topic; only observe records produced from here on.
        // position() forces the lazy seekToEnd to resolve now, before any record is sent.
        errorLogConsumer.seekToEnd(errorLogConsumer.assignment());
        errorLogConsumer.assignment().forEach(errorLogConsumer::position);
    }

    @AfterEach
    void close() {
        errorLogConsumer.close();
    }

    @Test
    void errorLogsAreRoutedToErrorTopic() throws Exception {
        producer.send(new LogMessage("ERROR", System.currentTimeMillis(), "boom"));

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(errorLogConsumer, KafkaTopics.ERROR_LOGS, Duration.ofSeconds(10));

        LogMessage routed = objectMapper.readValue(record.value(), LogMessage.class);
        assertThat(routed.getLevel()).isEqualTo("ERROR");
        assertThat(routed.getMessage()).isEqualTo("boom");
    }

    @Test
    void nonErrorLogsAreNotRouted() throws Exception {
        producer.send(new LogMessage("INFO", System.currentTimeMillis(), "all good"));

        assertThat(KafkaTestUtils.getRecords(errorLogConsumer, Duration.ofSeconds(3)).isEmpty()).isTrue();
    }
}
