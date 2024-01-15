package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaIntegrationTest {
    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    private static final String TOPIC_NAME = "test-topic";

    @BeforeAll
    public static void startKafkaContainer() {
        kafkaContainer.start();
    }

    @AfterAll
    public static void stopKafkaContainer() {
        kafkaContainer.stop();
    }

    @Test
    @DisplayName("testAtMostOnceSemantics - Produced 3 messages - received 3 messages")
    @Order(1)
    public void testAtLeastOnceSemantics() {
        String bootstrapServers = kafkaContainer.getBootstrapServers();

        produceMessages(bootstrapServers, TOPIC_NAME, "Message A", "Message B", "Message C");

        Set<String> receivedMessages = consumeMessagesAtLeastOnceSemantics(bootstrapServers, TOPIC_NAME);

        Assertions.assertEquals(3, receivedMessages.size());
        Assertions.assertTrue(receivedMessages.contains("Message A"));
        Assertions.assertTrue(receivedMessages.contains("Message B"));
        Assertions.assertTrue(receivedMessages.contains("Message C"));
    }

    @Test
    @DisplayName("testAtMostOnceSemantics - Produced 3 messages - received 3 messages")
    @Order(2)
    public void testAtMostOnceSemantics() {
        String bootstrapServers = kafkaContainer.getBootstrapServers();

        produceMessages(bootstrapServers, TOPIC_NAME, "Message A", "Message B", "Message C");

        Set<String> receivedMessages = consumeMessagesAtMostOnceSemantics(bootstrapServers, TOPIC_NAME);

        Assertions.assertEquals(3, receivedMessages.size());
        Assertions.assertTrue(receivedMessages.contains("Message A"));
        Assertions.assertTrue(receivedMessages.contains("Message B"));
        Assertions.assertTrue(receivedMessages.contains("Message C"));
    }

    private void produceMessages(String bootstrapServers, String topic, String... messages) {
        Properties producerProperties = new Properties();
        producerProperties.put("bootstrap.servers", bootstrapServers);
        producerProperties.put("acks", "all");
        producerProperties.put("retries", 3);
        producerProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(producerProperties)) {
            for (String message : messages) {
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, UUID.randomUUID().toString(), message);
                try {
                    producer.send(record).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Set<String> consumeMessagesAtLeastOnceSemantics(String bootstrapServers, String topic) {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "CONSUMER_APP_ID");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "CONSUMER_GROUP_ID");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Collections.singletonList(topic));

        Set<String> receivedMessages = new HashSet<>();
        try {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            for (ConsumerRecord<String, String> record : records) {
                receivedMessages.add(record.value());
            }

            consumer.commitAsync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }

        return receivedMessages;
    }

    private Set<String> consumeMessagesAtMostOnceSemantics(String bootstrapServers, String topic) {
        Properties consumerProperties = new Properties();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "CONSUMER_APP_ID");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "CONSUMER_GROUP_ID");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Collections.singletonList(topic));

        Set<String> receivedMessages = new HashSet<>();

        try {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            for (ConsumerRecord<String, String> record : records) {
                receivedMessages.add(record.value());
            }

            consumer.commitAsync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }

        return receivedMessages;
    }
}
