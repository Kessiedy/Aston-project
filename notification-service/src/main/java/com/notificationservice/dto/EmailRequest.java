package com.notificationservice.dto;

public class EmailRequest {
    private String email;
    private String operation;
    private String name;

    public EmailRequest() {
    }

    public EmailRequest(String email, String operation, String name) {
        this.email = email;
        this.operation = operation;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}