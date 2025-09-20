package com.navexa.secretbuddy.api.rest;

import com.navexa.secretbuddy.core.model.Event;
import com.navexa.secretbuddy.core.model.Participant;
import com.navexa.secretbuddy.core.service.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.navexa.secretbuddy.api.dto.EventDtos.CreateEventRequest;
import com.navexa.secretbuddy.api.dto.EventDtos.CreateEventResponse;
import com.navexa.secretbuddy.api.dto.ParticipantDtos.AddParticipantsBulkRequest;
import com.navexa.secretbuddy.api.dto.ParticipantDtos.ParticipantView;
import com.navexa.secretbuddy.core.service.EventService.ParticipantInput;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    @Value("${frontend.origin}")
    private String frontendBaseUrl;


    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateEventResponse create(@RequestBody @Validated CreateEventRequest req) {
        Event e = eventService.createEvent(req.name());
        return new CreateEventResponse(e.getId().toString(), e.getName(), e.getStatus().name());
    }


    @PostMapping("/{eventId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ParticipantView> addBulk(@PathVariable(name = "eventId") UUID eventId,
                                         @RequestBody @Validated AddParticipantsBulkRequest req) {
        List<Participant> saved = eventService.addParticipantsBulk(eventId,
                req.items().stream().map(i -> new ParticipantInput(i.name(), i.phone())).toList());
        return saved.stream().map(p -> {
            String link = ServletUriComponentsBuilder
                    .fromHttpUrl(frontendBaseUrl)
                    .path("/join/{eventId}/{token}")
                    .buildAndExpand(eventId.toString(), p.getJoinToken())
                    .toUriString();
            return new ParticipantView(p.getId().toString(), p.getName(), p.getJoinToken(), link);
        }).toList();

    }

    @GetMapping("/{eventId}/{joinToken}/participants")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipantView> getAllParticipants(@PathVariable(name = "eventId") UUID eventId,
                                                    @PathVariable(name = "joinToken") String joinToken) {
        var participants = eventService.getAllParticipants(eventId, joinToken);

        return participants.stream().map(p ->
            new ParticipantView(p.getId().toString(), p.getName(), null, null)
        ).toList();
    }

    @PostMapping("/{eventId}/ready")
    public CreateEventResponse setReady(@PathVariable(name = "eventId") UUID eventId) {
        Event e = eventService.setReady(eventId);
        return new CreateEventResponse(e.getId().toString(), e.getName(), e.getStatus().name());
    }
}