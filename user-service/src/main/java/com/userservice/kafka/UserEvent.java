package com.userservice.kafka;

import java.time.LocalDateTime;

public class UserEvent {
    private String eventType;
    private String name;
    private String email;
    private Long userId;
    private Integer age;
    private LocalDateTime timestamp;

    public UserEvent(){}

    public UserEvent(String eventType, Long userId, String email, String name, Integer age) {
        this.eventType = eventType;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.age = age;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventType() {
        return eventType;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getAge() {
        return age;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "eventType='" + eventType + '\'' +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", timestamp=" + timestamp +
                '}';
    }
}