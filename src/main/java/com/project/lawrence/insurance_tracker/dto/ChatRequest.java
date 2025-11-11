package com.project.lawrence.insurance_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatRequest {

    public String model;
    public Message[] messages;
    public Double temperature;

    public ChatRequest(String model, Message[] messages, Double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

}
