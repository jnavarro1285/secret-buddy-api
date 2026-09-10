package com.navexa.secretbuddy.core.service;

import com.navexa.secretbuddy.core.model.Participant;
import com.navexa.secretbuddy.core.repository.AssignmentRepository;
import com.navexa.secretbuddy.core.repository.EventRepository;
import com.navexa.secretbuddy.core.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RevealServiceTest {

    private RevealService revealService;
    private ParticipantRepository participantRepo;
    private AssignmentRepository assignmentRepo;
    private EventRepository eventRepo;

    @BeforeEach
    void setUp() {
        participantRepo = Mockito.mock(ParticipantRepository.class);
        assignmentRepo = Mockito.mock(AssignmentRepository.class);
        eventRepo = Mockito.mock(EventRepository.class);
        revealService = new RevealService(participantRepo, assignmentRepo, eventRepo);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFilterEligible_Case1_AllNoExclusions() throws Exception {
        Participant giver = new Participant();
        giver.setId(UUID.randomUUID());
        giver.setPhoneE164("+111");
        giver.setPermittedParticipants("ALL");
        giver.setExcludedParticipants("");

        Participant p2 = new Participant();
        p2.setId(UUID.randomUUID());
        p2.setPhoneE164("+222");

        List<Participant> candidates = new ArrayList<>(List.of(p2));

        Method method = RevealService.class.getDeclaredMethod("filterEligible", Participant.class, List.class);
        method.setAccessible(true);
        List<Participant> result = (List<Participant>) method.invoke(revealService, giver, candidates);

        assertEquals(1, result.size());
        assertEquals(p2.getId(), result.get(0).getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFilterEligible_Case2_AllWithExclusions() throws Exception {
        Participant giver = new Participant();
        giver.setId(UUID.randomUUID());
        giver.setPermittedParticipants("ALL");
        giver.setExcludedParticipants("+222,+333");

        Participant p2 = new Participant();
        p2.setId(UUID.randomUUID());
        p2.setPhoneE164("+222");

        Participant p3 = new Participant();
        p3.setId(UUID.randomUUID());
        p3.setPhoneE164("+333");

        Participant p4 = new Participant();
        p4.setId(UUID.randomUUID());
        p4.setPhoneE164("+444");

        List<Participant> candidates = new ArrayList<>(List.of(p2, p3, p4));

        Method method = RevealService.class.getDeclaredMethod("filterEligible", Participant.class, List.class);
        method.setAccessible(true);
        List<Participant> result = (List<Participant>) method.invoke(revealService, giver, candidates);

        assertEquals(1, result.size());
        assertEquals(p4.getId(), result.get(0).getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFilterEligible_Case3_SpecificPermitted() throws Exception {
        Participant giver = new Participant();
        giver.setId(UUID.randomUUID());
        giver.setPermittedParticipants("+222,+333");
        giver.setExcludedParticipants("");

        Participant p2 = new Participant();
        p2.setId(UUID.randomUUID());
        p2.setPhoneE164("+222");

        Participant p4 = new Participant();
        p4.setId(UUID.randomUUID());
        p4.setPhoneE164("+444");

        List<Participant> candidates = new ArrayList<>(List.of(p2, p4));

        Method method = RevealService.class.getDeclaredMethod("filterEligible", Participant.class, List.class);
        method.setAccessible(true);
        List<Participant> result = (List<Participant>) method.invoke(revealService, giver, candidates);

        assertEquals(1, result.size());
        assertEquals(p2.getId(), result.get(0).getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFilterEligible_Case4_ExclusionOverridesPermission() throws Exception {
        Participant giver = new Participant();
        giver.setId(UUID.randomUUID());
        giver.setPermittedParticipants("+222,+333");
        giver.setExcludedParticipants("+222");

        Participant p2 = new Participant();
        p2.setId(UUID.randomUUID());
        p2.setPhoneE164("+222");

        Participant p3 = new Participant();
        p3.setId(UUID.randomUUID());
        p3.setPhoneE164("+333");

        List<Participant> candidates = new ArrayList<>(List.of(p2, p3));

        Method method = RevealService.class.getDeclaredMethod("filterEligible", Participant.class, List.class);
        method.setAccessible(true);
        List<Participant> result = (List<Participant>) method.invoke(revealService, giver, candidates);

        assertEquals(1, result.size());
        assertEquals(p3.getId(), result.get(0).getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFilterEligible_CaseSelfExclusion() throws Exception {
        Participant giver = new Participant();
        giver.setId(UUID.randomUUID());
        giver.setPermittedParticipants("ALL");
        giver.setExcludedParticipants("");

        List<Participant> candidates = new ArrayList<>(List.of(giver));

        Method method = RevealService.class.getDeclaredMethod("filterEligible", Participant.class, List.class);
        method.setAccessible(true);
        List<Participant> result = (List<Participant>) method.invoke(revealService, giver, candidates);

        assertTrue(result.isEmpty());
    }
}
