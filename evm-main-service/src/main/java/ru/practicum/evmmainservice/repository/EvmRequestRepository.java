package ru.practicum.evmmainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.evmmainservice.entity.Request;

import java.util.List;
import java.util.Optional;

public interface EvmRequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByRequester_IdAndEvent_Id(Long requesterId, Long eventId);

    @Query("select r from Request r where r.event.id = :eventId and r.status = 'CONFIRMED'")
    List<Request> findAllByEventIdAndStatusConfirmed(Long eventId);

    @Query("select r from Request r where r.event.id in :ids and r.status = 'CONFIRMED'")
    List<Request> findAllByEventIdsAndStatusConfirmed(List<Long> ids);

    List<Request> findAllByRequesterId(Long requesterId);

    List<Request> findAllByEventId(Long eventId);
}
