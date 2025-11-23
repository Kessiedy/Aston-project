package com.notificationservice.controller;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.EmailResponse;
import com.notificationservice.event.UserEvent;
import com.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendNotification(@RequestBody EmailRequest request) {
        logger.info("Получен запрос на отправку уведомления: email={}, operation={}",
                request.getEmail(), request.getOperation());

        try {
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Email не может быть пустым"));
            }

            if (request.getOperation() == null || request.getOperation().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Операция не может быть пустой"));
            }

            UserEvent event = new UserEvent();
            event.setEmail(request.getEmail());
            event.setName(request.getName() != null ? request.getName() : "Пользователь");
            event.setEventType(request.getOperation().toUpperCase());

            if ("CREATE".equalsIgnoreCase(request.getOperation())) {
                emailService.sendUserCreatedEmail(event);
                return ResponseEntity.ok(
                        new EmailResponse(true, "Уведомление о создании аккаунта успешно отправлено на " + request.getEmail())
                );
            } else if ("DELETE".equalsIgnoreCase(request.getOperation())) {
                emailService.sendUserDeletedEmail(event);
                return ResponseEntity.ok(
                        new EmailResponse(true, "Уведомление об удалении аккаунта успешно отправлено на " + request.getEmail())
                );
            } else {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Неизвестная операция: " + request.getOperation() + ". Используйте CREATE или DELETE"));
            }

        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EmailResponse(false, "Ошибка при отправке email: " + e.getMessage()));
        }
    }
}