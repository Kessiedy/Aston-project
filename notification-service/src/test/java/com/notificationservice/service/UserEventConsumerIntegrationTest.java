package com.notificationservice.service;

import com.icegreen.greenmail.store.FolderException;
import com.notificationservice.config.TestEmailConfig;
import com.notificationservice.config.TestKafkaProducerConfig;
import com.notificationservice.event.UserEvent;
import com.icegreen.greenmail.util.GreenMail;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-user-events"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093",
                "port=9093"
        }
)
@DirtiesContext
@Import({TestKafkaProducerConfig.class, TestEmailConfig.class})
class UserEventConsumerIntegrationTest {

    @Autowired
    private GreenMail greenMail;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${kafka.topic.user-events}")
    private String topicName;

    @AfterEach
    void tearDown() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void testConsumeUserCreatedEvent() {
        UserEvent event = new UserEvent();
        event.setEventType("USER_CREATED");
        event.setUserId(1L);
        event.setEmail("kafkauser@example.com");
        event.setName("Kafka User");
        event.setAge(28);

        kafkaTemplate.send(topicName, event.getUserId().toString(), event);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSizeGreaterThanOrEqualTo(1);

            MimeMessage lastMessage = receivedMessages[receivedMessages.length - 1];
            assertThat(lastMessage.getAllRecipients()[0].toString()).isEqualTo("kafkauser@example.com");
            assertThat(lastMessage.getSubject()).contains("Добро пожаловать");
        });
    }

    @Test
    void testConsumeUserDeletedEvent() {
        UserEvent event = new UserEvent();
        event.setEventType("USER_DELETED");
        event.setUserId(2L);
        event.setEmail("deletedkafkauser@example.com");
        event.setName("Deleted Kafka User");
        event.setAge(35);

        kafkaTemplate.send(topicName, event.getUserId().toString(), event);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSizeGreaterThanOrEqualTo(1);

            MimeMessage lastMessage = receivedMessages[receivedMessages.length - 1];
            assertThat(lastMessage.getAllRecipients()[0].toString()).isEqualTo("deletedkafkauser@example.com");
            assertThat(lastMessage.getSubject()).contains("удален");
        });
    }
}
