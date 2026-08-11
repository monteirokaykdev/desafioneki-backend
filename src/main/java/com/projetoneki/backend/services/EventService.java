package com.projetoneki.backend.services;

import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.projetoneki.backend.dto.Response.EventResponse;
import com.projetoneki.backend.exception.ForbiddenException;
import com.projetoneki.backend.exception.InvalidImageException;
import com.projetoneki.backend.exception.InvalidRequestException;
import com.projetoneki.backend.exception.ResourceNotFoundException;
import com.projetoneki.backend.model.Admin;
import com.projetoneki.backend.model.Event;
import com.projetoneki.backend.repository.AdminRepository;
import com.projetoneki.backend.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AdminRepository adminRepository;

    public EventResponse create(
            Event event,
            Long adminId,
            MultipartFile image) {

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrador não encontrado"
                        )
                );

        event.setAdmin(admin);

        if (image != null && !image.isEmpty()) {

            validateImage(image);

            try {
                event.setImage(image.getBytes());
                event.setImageType(image.getContentType());

            } catch (IOException e) {
                throw new RuntimeException(
                        "Erro ao processar imagem",
                        e
                );
            }
        }

        Event savedEvent = eventRepository.save(event);

        return toResponse(savedEvent);
    }

    public List<EventResponse> findByAdmin(Long adminId) {

        return eventRepository.findByAdminId(adminId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse findById(Long eventId, Long adminId) {

        Event event = findEntityByIdForOwner(eventId, adminId);

        return toResponse(event);
    }

    public Event findEntityById(Long eventId) {

        return eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento não encontrado"
                        )
                );
    }

    /**
     * Busca o evento garantindo que ele pertence ao admin informado.
     * Usado por todos os endpoints que operam em um evento específico
     * (findById, getImage, update, delete) para impedir que um admin
     * acesse/edite/apague eventos de outro admin.
     */
    public Event findEntityByIdForOwner(Long eventId, Long adminId) {

        Event event = findEntityById(eventId);

        if (!event.getAdmin().getId().equals(adminId)) {
            throw new ForbiddenException(
                    "Você não tem permissão para acessar este evento"
            );
        }

        return event;
    }

    private void validateImage(MultipartFile image) {

        if (image == null || image.isEmpty()) {
            return;
        }

        String contentType = image.getContentType();

        if (!"image/png".equals(contentType)
                && !"image/jpeg".equals(contentType)) {

            throw new InvalidImageException(
                    "Apenas imagens PNG ou JPG são permitidas"
            );
        }

        if (image.getSize() > 5 * 1024 * 1024) {

            throw new InvalidImageException(
                    "A imagem deve ter no máximo 5MB"
            );
        }
    }

        public EventResponse update(
            Long eventId,
            LocalDateTime date,
            String location,
            MultipartFile image,
            Long adminId) {

        Event event = findEntityByIdForOwner(eventId, adminId);

        if (date == null
                && location == null
                && (image == null || image.isEmpty())) {

            throw new InvalidRequestException(
                    "Informe ao menos a data, a localização ou uma nova imagem para atualizar"
            );
        }

        if (date != null) {
            event.setDate(date);
        }

        if (location != null && !location.isBlank()) {
            event.setLocation(location);
        }

        if (image != null && !image.isEmpty()) {

            validateImage(image);

            try {
                event.setImage(image.getBytes());
                event.setImageType(image.getContentType());

            } catch (IOException e) {
                throw new RuntimeException(
                        "Erro ao processar imagem",
                        e
                );
            }
        }

        Event updatedEvent = eventRepository.save(event);

        return toResponse(updatedEvent);
    }

    public void delete(Long eventId, Long adminId) {

        Event event = findEntityByIdForOwner(eventId, adminId);

        eventRepository.delete(event);
    }

    private EventResponse toResponse(Event event) {

        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .date(event.getDate())
                .location(event.getLocation())
                .hasImage(event.getImage() != null)
                .build();
    }
}
