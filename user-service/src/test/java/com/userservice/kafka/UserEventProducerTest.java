package com.userservice.kafka;

import com.userservice.kafka.UserEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-user-events"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9094",
                "port=9094"
        }
)
@DirtiesContext
class UserEventProducerTest {

    @Autowired
    private UserEventProducer userEventProducer;

    @Value("${kafka.topic.user-events}")
    private String topicName;

    private KafkaMessageListenerContainer<String, UserEvent> container;
    private BlockingQueue<ConsumerRecord<String, UserEvent>> records;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, UserEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps,
                        new StringDeserializer(),
                        new JsonDeserializer<>(UserEvent.class, false));

        ContainerProperties containerProperties = new ContainerProperties(topicName);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, UserEvent>) records::add);
        container.start();

        ContainerTestUtils.waitForAssignment(container, 1);
    }

    @AfterEach
    void tearDown() {
        container.stop();
    }

    @Test
    void testSendUserCreatedEvent() throws InterruptedException {
        // Given
        UserEvent event = new UserEvent(
                "USER_CREATED",
                1L,
                "test@example.com",
                "Test User",
                25
        );

        // When
        userEventProducer.sendUserCreatedEvent(event);

        // Then
        ConsumerRecord<String, UserEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo("1");
        assertThat(received.value().getEventType()).isEqualTo("USER_CREATED");
        assertThat(received.value().getEmail()).isEqualTo("test@example.com");
        assertThat(received.value().getName()).isEqualTo("Test User");
        assertThat(received.value().getAge()).isEqualTo(25);
    }

    @Test
    void testSendUserDeletedEvent() throws InterruptedException {
        // Given
        UserEvent event = new UserEvent(
                "USER_DELETED",
                2L,
                "deleted@example.com",
                "Deleted User",
                30
        );

        // When
        userEventProducer.sendUserDeletedEvent(event);

        // Then
        ConsumerRecord<String, UserEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo("2");
        assertThat(received.value().getEventType()).isEqualTo("USER_DELETED");
        assertThat(received.value().getEmail()).isEqualTo("deleted@example.com");
        assertThat(received.value().getName()).isEqualTo("Deleted User");
    }
}