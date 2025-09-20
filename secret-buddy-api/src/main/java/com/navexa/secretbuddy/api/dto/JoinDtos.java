package com.navexa.secretbuddy.api.dto;

public class JoinDtos {
    public record JoinContext(String participantId, String participantName, boolean alreadyRevealed,
                              String receiverName) {
    }

    public record RevealResponse(String receiverId, String receiverName) {
    }
}