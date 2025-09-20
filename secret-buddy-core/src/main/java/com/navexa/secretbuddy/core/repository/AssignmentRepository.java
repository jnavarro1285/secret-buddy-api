package com.navexa.secretbuddy.core.repository;

import com.navexa.secretbuddy.core.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;
import java.util.UUID;


public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    @Query("select a from Assignment a where a.event.id = :eventId and a.giver.id = :giverId")
    Optional<Assignment> findByEventAndGiver(@Param("eventId") UUID eventId,
                                             @Param("giverId") UUID giverId);
}