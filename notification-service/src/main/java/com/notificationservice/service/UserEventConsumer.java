package com.notificationservice.service;

import com.notificationservice.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);

    private final EmailService emailService;

    public UserEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${kafka.topic.user-events}", groupId = "notification-service-group")
    public void consume(UserEvent event) {
        logger.info("Получено событие из Kafka: {}", event);

        try {
            if ("USER_CREATED".equals(event.getEventType())) {
                emailService.sendUserCreatedEmail(event);
            } else if ("USER_DELETED".equals(event.getEventType())) {
                emailService.sendUserDeletedEmail(event);
            } else {
                logger.warn("Неизвестный тип события: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Ошибка при обработке события: {}", e.getMessage(), e);
        }
    }
}