package ru.practicum.evmmainservice.repository;

import ru.practicum.evmmainservice.enumEwm.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmmainservice.entity.Request;

import java.util.List;
import java.util.Optional;

public interface EvmRequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByRequester_IdAndEvent_Id(Long requesterId, Long eventId);

    List<Request> findAllByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByRequester_Id(Long requesterId);

    List<Request> findAllByEvent_Id(Long eventId);
}
