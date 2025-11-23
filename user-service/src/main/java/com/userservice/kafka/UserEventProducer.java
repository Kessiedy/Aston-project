package com.userservice.kafka;

import com.userservice.kafka.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);

    @Value("${kafka.topic.user-events}")
    private String topicName;

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserCreatedEvent(UserEvent event) {
        logger.info("Отправка события USER_CREATED для пользователя: {}", event.getEmail());
        kafkaTemplate.send(topicName, event.getUserId().toString(), event);
    }

    public void sendUserDeletedEvent(UserEvent event) {
        logger.info("Отправка события USER_DELETED для пользователя: {}", event.getEmail());
        kafkaTemplate.send(topicName, event.getUserId().toString(), event);
    }
}