package com.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping(value = "/user-service", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        logger.warn("User Service fallback активирован");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("error", "Service Unavailable");
        response.put("message", "User Service временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("path", "/api/users");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @RequestMapping(value = "/notification-service", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        logger.warn("Notification Service fallback активирован");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("error", "Service Unavailable");
        response.put("message", "Notification Service временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("path", "/api/notifications");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}