package com.navexa.secretbuddy.core.repository;

import com.navexa.secretbuddy.core.model.Participant;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    Optional<Participant> findByEventIdAndJoinToken(@Param("eventId") UUID eventId,
                                                    @Param("joinToken") String joinToken);

    Optional<List<Participant>> findByEventIdAndJoinTokenNot(@Param("eventId") UUID eventId, @Param("joinToken") String joinToken);

    /**
     * JPQL: devuelve ids (UUID) de participantes que aún no han sido asignados como receiver.
     * JPQL respeta el mapping de JPA por lo que devolerá List<UUID> correctamente.
     */
    @Query("select p.id from Participant p " +
            "where p.event.id = :eventId " +
            "and not exists (select 1 from Assignment a where a.event.id = :eventId and a.receiver.id = p.id) " +
            "order by p.id")
    List<UUID> findRemainingReceiverIds(@Param("eventId") UUID eventId);

    /**
     * Para bloquear una fila de participante de forma portable (PESSIMISTIC_WRITE).
     * Úsalo cuando hayas decidido candidateId y necesites asegurar que nadie más lo cambie.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Participant p where p.id = :participantId")
    Optional<Participant> findByIdForUpdate(@Param("participantId") UUID participantId);
}