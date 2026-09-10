package com.navexa.secretbuddy.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ParticipantDtos {
    public record AddParticipantRequest(@NotBlank String name,
                                        @NotBlank String phone,
                                        String permittedParticipants,
                                        String excludedParticipants) {
    }

    public record AddParticipantsBulkRequest(List<AddParticipantRequest> items) {
    }

    public record ParticipantView(String id,
                                   String name,
                                   String joinToken,
                                   String joinLink,
                                   String permittedParticipants,
                                   String excludedParticipants) {
    }
}