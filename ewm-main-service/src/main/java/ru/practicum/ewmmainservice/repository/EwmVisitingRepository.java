package ru.practicum.ewmmainservice.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmainservice.entity.Visiting;

import java.util.List;
import java.util.Optional;

public interface EwmVisitingRepository extends JpaRepository<Visiting, Long> {
    List<Visiting> findAllByVisitorId(Long visitorId, PageRequest pageRequest);

    Optional<Visiting> findByEventIdAndVisitorId(Long eventId, Long visitorId);
}
