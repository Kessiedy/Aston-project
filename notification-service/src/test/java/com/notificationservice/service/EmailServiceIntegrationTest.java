package com.notificationservice.service;

import com.icegreen.greenmail.store.FolderException;
import com.notificationservice.config.TestEmailConfig;
import com.notificationservice.event.UserEvent;
import com.icegreen.greenmail.util.GreenMail;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestEmailConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999"
})
class EmailServiceIntegrationTest {

    @Autowired
    private GreenMail greenMail;

    @Autowired
    private EmailService emailService;

    @AfterEach
    void tearDown() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void testSendUserCreatedEmail() throws Exception {
        UserEvent event = new UserEvent();
        event.setEventType("USER_CREATED");
        event.setUserId(1L);
        event.setEmail("testuser@example.com");
        event.setName("Test User");
        event.setAge(25);

        emailService.sendUserCreatedEmail(event);

        Thread.sleep(2000);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo("testuser@example.com");
        assertThat(receivedMessage.getSubject()).contains("Добро пожаловать");
    }

    @Test
    void testSendUserDeletedEmail() throws Exception {
        UserEvent event = new UserEvent();
        event.setEventType("USER_DELETED");
        event.setUserId(2L);
        event.setEmail("deleteduser@example.com");
        event.setName("Deleted User");
        event.setAge(30);

        emailService.sendUserDeletedEmail(event);

        Thread.sleep(2000);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo("deleteduser@example.com");
        assertThat(receivedMessage.getSubject()).contains("удален");
    }
}