package com.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class EmailConfig {

    @PostConstruct
    public void init() {
        System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        System.setProperty("mail.smtp.ssl.checkserveridentity", "false");
        System.setProperty("mail.smtp.ssl.trust", "*");
    }
}