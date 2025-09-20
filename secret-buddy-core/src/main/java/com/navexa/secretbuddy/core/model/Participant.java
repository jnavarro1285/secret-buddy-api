package com.navexa.secretbuddy.core.model;

import jakarta.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "participant",
        uniqueConstraints = {@UniqueConstraint(name = "uk_participant_token", columnNames = {"join_token"})})
public class Participant {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_e164", nullable = false)
    private String phoneE164;

    @Column(name = "phone_hash", nullable = false)
    private String phoneHash;

    @Column(name = "join_token", nullable = false, unique = true, length = 64)
    private String joinToken;


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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneE164() {
        return phoneE164;
    }

    public void setPhoneE164(String phoneE164) {
        this.phoneE164 = phoneE164;
    }

    public String getPhoneHash() {
        return phoneHash;
    }

    public void setPhoneHash(String phoneHash) {
        this.phoneHash = phoneHash;
    }

    public String getJoinToken() {
        return joinToken;
    }

    public void setJoinToken(String joinToken) {
        this.joinToken = joinToken;
    }
}