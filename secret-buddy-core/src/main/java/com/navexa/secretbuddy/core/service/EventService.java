package com.navexa.secretbuddy.core.service;

import com.navexa.secretbuddy.core.model.Event;
import com.navexa.secretbuddy.core.model.EventStatus;
import com.navexa.secretbuddy.core.model.Participant;
import com.navexa.secretbuddy.core.repository.EventRepository;
import com.navexa.secretbuddy.core.repository.ParticipantRepository;
import com.navexa.secretbuddy.core.utils.PhoneUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;


@Service
public class EventService {
    private final EventRepository eventRepo;
    private final ParticipantRepository participantRepo;
    private final TokenService tokenService;


    public EventService(EventRepository eventRepo, ParticipantRepository participantRepo, TokenService tokenService) {
        this.eventRepo = eventRepo;
        this.participantRepo = participantRepo;
        this.tokenService = tokenService;
    }


    public Event createEvent(String name) {
        Event e = new Event();
        e.setName(name);
        e.setStatus(EventStatus.DRAFT);
        return eventRepo.save(e);
    }


    @Transactional
    public Participant addParticipant(UUID eventId, String name, String phoneRaw, String permitted, String excluded) {
        Event ev = eventRepo.findById(eventId).orElseThrow();
        String e164 = PhoneUtil.normalizeE164(phoneRaw);
        Participant p = new Participant();
        p.setEvent(ev);
        p.setName(name);
        p.setPhoneE164(e164);
        p.setPhoneHash(PhoneUtil.sha256Base64(e164));
        p.setJoinToken(tokenService.issueJoinToken(ev.getId(), e164));
        p.setPermittedParticipants(normalizePhoneList(permitted, true));
        p.setExcludedParticipants(normalizePhoneList(excluded, false));
        return participantRepo.save(p);
    }


    @Transactional
    public List<Participant> addParticipantsBulk(UUID eventId, List<ParticipantInput> inputs) {
        return inputs.stream().map(in -> addParticipant(eventId, in.name(), in.phone(), in.permittedParticipants(), in.excludedParticipants())).toList();
    }

    private String normalizePhoneList(String input, boolean allowAll) {
        if (input == null || input.isBlank()) {
            return allowAll ? "ALL" : "";
        }
        if (allowAll && input.trim().equalsIgnoreCase("ALL")) {
            return "ALL";
        }
        String[] parts = input.split(",");
        List<String> normalized = new java.util.ArrayList<>();
        for (String part : parts) {
            String p = part.trim();
            if (!p.isEmpty()) {
                normalized.add(PhoneUtil.normalizeE164(p));
            }
        }
        return String.join(",", normalized);
    }

    public List<Participant> getAllParticipants(UUID eventId, String joinToken) {
        return participantRepo.findByEventIdAndJoinTokenNot(eventId, joinToken).get();
    }

    public Event setReady(UUID eventId) {
        Event e = eventRepo.findById(eventId).orElseThrow();
        e.setStatus(EventStatus.READY);
        return eventRepo.save(e);
    }


    public record ParticipantInput(String name, String phone, String permittedParticipants, String excludedParticipants) {
    }
}