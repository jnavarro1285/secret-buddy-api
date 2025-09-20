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

        // Obtenemos lista de candidatos disponibles (UUIDs) vía JPQL
        List<UUID> remaining = participantRepo.findRemainingReceiverIds(eventId);

        // Excluimos al propio giver si está presente
        remaining.removeIf(id -> id.equals(giver.getId()));

        if (remaining.isEmpty()) {
            throw new IllegalStateException("No available receivers (all assigned or only self remains).");
        }

        final UUID candidateId;
        if (remaining.size() == 1) {
            // Si queda solo 1 candidato, elegirlo (no hay riesgo de self porque ya excluimos)
            candidateId = remaining.get(0);
        } else {
            // Elige aleatorio de la lista
            Collections.shuffle(remaining);
            candidateId = remaining.get(0);
        }

        // Lock the chosen candidate row with PESSIMISTIC_WRITE (portable)
        Participant receiver = participantRepo.findByIdForUpdate(candidateId)
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
                // En caso de colisión, recomputar remaining y retry (fall back)
                remaining = participantRepo.findRemainingReceiverIds(eventId);
                remaining.removeIf(id -> id.equals(giver.getId()));
                remaining.removeIf(id -> id.equals(candidateId)); // candidate already taken
                if (remaining.isEmpty()) throw new IllegalStateException("No receivers left to assign after retry");
                Collections.shuffle(remaining);
                final UUID candidateId1 = remaining.get(0);
                receiver = participantRepo.findByIdForUpdate(candidateId1)
                        .orElseThrow(() -> new IllegalStateException("Candidate not found or locked on retry"));
            }
        }
    }

}