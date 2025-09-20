package com.navexa.secretbuddy.api.dto;

import jakarta.validation.constraints.NotBlank;


public class EventDtos {
    public record CreateEventRequest(@NotBlank String name) {
    }

    public record CreateEventResponse(String id, String name, String status) {
    }
}