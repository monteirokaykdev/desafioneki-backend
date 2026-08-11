package com.projetoneki.backend.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.projetoneki.backend.dto.Request.EventRequest;
import com.projetoneki.backend.dto.Request.EventUpdateRequest;
import com.projetoneki.backend.dto.Response.EventResponse;
import com.projetoneki.backend.exception.ForbiddenException;
import com.projetoneki.backend.model.Event;
import com.projetoneki.backend.security.AdminUserDetails;
import com.projetoneki.backend.services.EventService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

        @PostMapping(consumes = "multipart/form-data")
        public ResponseEntity<EventResponse> create(
                @Valid EventRequest request,
                @RequestParam(required = false) MultipartFile image,
                @AuthenticationPrincipal AdminUserDetails principal) {

        LocalDateTime date = LocalDateTime.parse(
                request.getDate(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        );

        Event event = Event.builder()
                .name(request.getName())
                .date(date)
                .location(request.getLocation())
                .build();

        EventResponse createdEvent = eventService.create(
                event,
                principal.getId(),
                image
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdEvent);
        }


    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<EventResponse>> findByAdmin(
            @PathVariable Long adminId,
            @AuthenticationPrincipal AdminUserDetails principal) {

        if (!adminId.equals(principal.getId())) {
            throw new ForbiddenException("Você só pode listar os eventos da sua própria conta");
        }

        return ResponseEntity.ok(
                eventService.findByAdmin(adminId)
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> findById(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AdminUserDetails principal) {

        return ResponseEntity.ok(
                eventService.findById(eventId, principal.getId())
        );
    }

    @GetMapping("/{eventId}/image")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AdminUserDetails principal) {

        Event event = eventService.findEntityByIdForOwner(eventId, principal.getId());

        if (event.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType =
                MediaType.parseMediaType(event.getImageType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(event.getImage());
    }

        @PutMapping(value = "/{eventId}",consumes = "multipart/form-data")
        public ResponseEntity<EventResponse> update(
                @PathVariable Long eventId,
                EventUpdateRequest request,
                @RequestParam(required = false) MultipartFile image,
                @AuthenticationPrincipal AdminUserDetails principal) {

        LocalDateTime date = null;

        if (request.getDate() != null && !request.getDate().isBlank()) {
                date = LocalDateTime.parse(
                        request.getDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                );
        }

        EventResponse updatedEvent = eventService.update(
                eventId,
                date,
                request.getLocation(),
                image,
                principal.getId()
        );

        return ResponseEntity.ok(updatedEvent);
        }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AdminUserDetails principal) {

        eventService.delete(eventId, principal.getId());

        return ResponseEntity.noContent().build();
    }
}
