package com.navexa.secretbuddy.api.rest;

import com.navexa.secretbuddy.core.model.Assignment;
import com.navexa.secretbuddy.core.model.Participant;
import com.navexa.secretbuddy.core.repository.AssignmentRepository;
import com.navexa.secretbuddy.core.repository.ParticipantRepository;
import com.navexa.secretbuddy.core.service.RevealService;
import org.springframework.web.bind.annotation.*;
import com.navexa.secretbuddy.api.dto.JoinDtos.JoinContext;
import com.navexa.secretbuddy.api.dto.JoinDtos.RevealResponse;

import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/join")
public class JoinController {
    private final ParticipantRepository participantRepo;
    private final AssignmentRepository assignmentRepo;
    private final RevealService revealService;


    public JoinController(ParticipantRepository participantRepo,
                          AssignmentRepository assignmentRepo,
                          RevealService revealService) {
        this.participantRepo = participantRepo;
        this.assignmentRepo = assignmentRepo;
        this.revealService = revealService;
    }


    @GetMapping("/{eventId}/{token}")
    public JoinContext getContext(@PathVariable(name = "eventId") UUID eventId, @PathVariable(name = "token") String token) {
        Participant p = participantRepo.findByEventIdAndJoinToken(eventId, token).orElseThrow();
        Optional<Assignment> a = assignmentRepo.findByEventAndGiver(eventId, p.getId());
        return new JoinContext(p.getId().toString(), p.getName(), a.isPresent(), a.map(x -> x.getReceiver().getName()).orElse(null));
    }


    @PostMapping("/{eventId}/{token}/reveal")
    public RevealResponse reveal(@PathVariable(name = "eventId") UUID eventId, @PathVariable(name = "token") String token) {
        var rr = revealService.reveal(eventId, token);
        return new RevealResponse(rr.receiverId().toString(), rr.receiverName());
    }
}