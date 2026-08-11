package com.projetoneki.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projetoneki.backend.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByAdminId(Long adminId);
}