package com.notificationservice.controller;

import com.icegreen.greenmail.store.FolderException;
import com.notificationservice.config.TestEmailConfig;
import com.notificationservice.dto.EmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMail;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestEmailConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999"
})
class NotificationControllerIntegrationTest {

    @Autowired
    private GreenMail greenMail;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification Service is running"));
    }

    @Test
    void testSendCreateNotification() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setEmail("newuser@example.com");
        request.setOperation("CREATE");
        request.setName("New User");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Уведомление о создании аккаунта успешно отправлено на newuser@example.com"));

        Thread.sleep(2000);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testSendDeleteNotification() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setEmail("olduser@example.com");
        request.setOperation("DELETE");
        request.setName("Old User");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Уведомление об удалении аккаунта успешно отправлено на olduser@example.com"));

        Thread.sleep(2000);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testSendNotificationWithInvalidOperation() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setEmail("user@example.com");
        request.setOperation("INVALID");
        request.setName("User");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Неизвестная операция: INVALID. Используйте CREATE или DELETE"));
    }

    @Test
    void testSendNotificationWithEmptyEmail() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setEmail("");
        request.setOperation("CREATE");
        request.setName("User");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email не может быть пустым"));
    }
}