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

        if (a.isPresent()) {
            return new JoinContext(p.getId().toString(), p.getName(), a.get().isRevealed(), a.get().getReceiver().getName());
        } else {
            return new JoinContext(p.getId().toString(), p.getName(), false, null);
        }
    }


    @PostMapping("/{eventId}/{token}/reveal")
    public RevealResponse reveal(@PathVariable(name = "eventId") UUID eventId, @PathVariable(name = "token") String token) {
        var rr = revealService.reveal(eventId, token);
        return new RevealResponse(rr.receiverId().toString(), rr.receiverName());
    }

    @PutMapping("/{eventId}/{token}/reveal")
    public void updateAssignment(@PathVariable(name = "eventId") UUID eventId, @PathVariable(name = "token") String token) {
        revealService.updateAssignment(eventId, token);
    }
}