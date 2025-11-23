package com.notificationservice.service;

import com.notificationservice.event.UserEvent;
import com.notificationservice.model.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendUserCreatedEmail(UserEvent event) {
        try {
            Context context = new Context();
            context.setVariable("name", event.getName());
            context.setVariable("email", event.getEmail());
            context.setVariable("age", event.getAge());
            context.setVariable("userId", event.getUserId());

            String htmlContent = templateEngine.process("user-created-email", context);

            EmailDetails emailDetails = new EmailDetails(
                    event.getEmail(),
                    "Добро пожаловать! Ваш аккаунт создан",
                    htmlContent
            );

            sendHtmlEmail(emailDetails);
            logger.info("Письмо о создании пользователя отправлено на: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Ошибка при отправке письма о создании пользователя: {}", e.getMessage());
        }
    }

    public void sendUserDeletedEmail(UserEvent event) {
        try {
            Context context = new Context();
            context.setVariable("name", event.getName());
            context.setVariable("email", event.getEmail());

            String htmlContent = templateEngine.process("user-deleted-email", context);

            EmailDetails emailDetails = new EmailDetails(
                    event.getEmail(),
                    "Ваш аккаунт был удален",
                    htmlContent
            );

            sendHtmlEmail(emailDetails);
            logger.info("Письмо об удалении пользователя отправлено на: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Ошибка при отправке письма об удалении пользователя: {}", e.getMessage());
        }
    }

    private void sendHtmlEmail(EmailDetails emailDetails) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(emailDetails.getRecipient());
        helper.setSubject(emailDetails.getSubject());
        helper.setText(emailDetails.getBody(), true);

        try {
            mailSender.send(mimeMessage);
            logger.info("Email успешно отправлен на: {}", emailDetails.getRecipient());
        } catch (Exception e) {
            logger.error("Ошибка отправки email: {}", e.getMessage(), e);
            throw e;
        }
    }
}