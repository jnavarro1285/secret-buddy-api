package com.navexa.secretbuddy.core.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assign_giver", columnNames = {"event_id", "giver_id"}),
                @UniqueConstraint(name = "uk_assign_receiver", columnNames = {"event_id", "receiver_id"})
        })
public class Assignment {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giver_id", nullable = false)
    private Participant giver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Participant receiver;

    @Column(name = "revealed", nullable = false)
    private boolean revealed = true; // on-demand reveal happens immediately

    @Column(name = "revealed_at", nullable = false)
    private OffsetDateTime revealedAt = OffsetDateTime.now();


    // getters/setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Participant getGiver() {
        return giver;
    }

    public void setGiver(Participant giver) {
        this.giver = giver;
    }

    public Participant getReceiver() {
        return receiver;
    }

    public void setReceiver(Participant receiver) {
        this.receiver = receiver;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public OffsetDateTime getRevealedAt() {
        return revealedAt;
    }

    public void setRevealedAt(OffsetDateTime revealedAt) {
        this.revealedAt = revealedAt;
    }
}