package com.navexa.secretbuddy.core.service;

import com.navexa.secretbuddy.core.model.Assignment;
import com.navexa.secretbuddy.core.model.Event;
import com.navexa.secretbuddy.core.model.Participant;
import com.navexa.secretbuddy.core.repository.AssignmentRepository;
import com.navexa.secretbuddy.core.repository.EventRepository;
import com.navexa.secretbuddy.core.repository.ParticipantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.OffsetDateTime;
import java.util.*;


@Service
public class RevealService {
    private final ParticipantRepository participantRepo;
    private final AssignmentRepository assignmentRepo;
    private final EventRepository eventRepo;


    public RevealService(ParticipantRepository participantRepo,
                         AssignmentRepository assignmentRepo,
                         EventRepository eventRepo) {
        this.participantRepo = participantRepo;
        this.assignmentRepo = assignmentRepo;
        this.eventRepo = eventRepo;
    }


    public record RevealResult(UUID receiverId, String receiverName) {
    }


    @Transactional
    public RevealResult reveal(UUID eventId, String joinToken) {
        Event ev = eventRepo.findById(eventId).orElseThrow();
        Participant giver = participantRepo.findByEventIdAndJoinToken(eventId, joinToken).orElseThrow();

        // Idempotency: si ya existe la asignación la devolvemos
        Optional<Assignment> existing = assignmentRepo.findByEventAndGiver(eventId, giver.getId());
        if (existing.isPresent()) {
            Participant r = existing.get().getReceiver();
            return new RevealResult(r.getId(), r.getName());
        }

        // Obtenemos lista de candidatos disponibles (Participantes completos)
        List<Participant> candidates = participantRepo.findAllById(participantRepo.findRemainingReceiverIds(eventId));

        // Aplicamos reglas de negocio para filtrar candidatos
        List<Participant> eligible = filterEligible(giver, candidates);
        List<Participant> shuffledEligible = new ArrayList<>(eligible);

        if (eligible.isEmpty()) {
            throw new IllegalStateException("No available receivers (restrictions leave no valid candidates).");
        }

        final Participant chosen;
        if (eligible.size() == 1) {
            chosen = shuffledEligible.get(0);
        } else {
            Collections.shuffle(shuffledEligible);
            chosen = shuffledEligible.get(0);
        }

        // Lock the chosen candidate row with PESSIMISTIC_WRITE (portable)
        Participant receiver = participantRepo.findByIdForUpdate(chosen.getId())
                .orElseThrow(() -> new IllegalStateException("Candidate not found or locked"));

        // Persist assignment; si hay colisión por concurrent insert (edge case), reintentar
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                Assignment a = new Assignment();
                a.setEvent(ev);
                a.setGiver(giver);
                a.setReceiver(receiver);
                a.setRevealed(true);
                a.setRevealedAt(OffsetDateTime.now());
                assignmentRepo.saveAndFlush(a);
                return new RevealResult(receiver.getId(), receiver.getName());
            } catch (DataIntegrityViolationException ex) {
                if (attempts >= 5) throw ex;
                // En caso de colisión, recomputar remaining y retry
                List<Participant> remaining = participantRepo.findAllById(participantRepo.findRemainingReceiverIds(eventId));
                List<Participant> retryEligible = filterEligible(giver, remaining);
                if (retryEligible.isEmpty()) throw new IllegalStateException("No receivers left to assign after retry");
                Collections.shuffle(retryEligible);
                final Participant chosenRetry = retryEligible.get(0);
                receiver = participantRepo.findByIdForUpdate(chosenRetry.getId())
                        .orElseThrow(() -> new IllegalStateException("Candidate not found or locked on retry"));
            }
        }
    }

    @Transactional
    public void updateAssignment(UUID eventId, String joinToken) {
        Event ev = eventRepo.findById(eventId).orElseThrow();
        Participant giver = participantRepo.findByEventIdAndJoinToken(eventId, joinToken).orElseThrow();

        Optional<Assignment> assignmentOptional = assignmentRepo.findByEventAndGiver(eventId, giver.getId());

        if (assignmentOptional.isPresent()) {
            var a = assignmentOptional.get();
            a.setRevealed(true);
            a.setRevealedAt(OffsetDateTime.now());
            assignmentRepo.save(a);
        }
    }

    private List<Participant> filterEligible(Participant giver, List<Participant> candidates) {
        Set<String> excluded = parsePhoneList(giver.getExcludedParticipants());
        String permittedStr = giver.getPermittedParticipants();
        boolean allPermitted = "ALL".equalsIgnoreCase(permittedStr);
        Set<String> permitted = allPermitted ? Set.of() : parsePhoneList(permittedStr);

        return candidates.stream()
                .filter(c -> !c.getId().equals(giver.getId())) // Cannot draw self
                .filter(c -> !excluded.contains(c.getPhoneE164())) // RN1: Exclusion always wins
                .filter(c -> allPermitted || permitted.contains(c.getPhoneE164())) // RN1: Permission second
                .toList();
    }

    private Set<String> parsePhoneList(String list) {
        if (list == null || list.isBlank()) return Set.of();
        return new HashSet<>(Arrays.asList(list.split(",")));
    }

}