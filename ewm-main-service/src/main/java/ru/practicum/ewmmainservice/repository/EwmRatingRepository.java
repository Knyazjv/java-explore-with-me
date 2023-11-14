package ru.practicum.ewmmainservice.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmainservice.entity.Rating;

import java.util.List;
import java.util.Optional;

public interface EwmRatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findAllByEventId(Long eventId);

    List<Rating> findAllByEstimatorId(Long userId);

    List<Rating> findAllByEstimatorId(Long userId, PageRequest page);

    Optional<Rating> findByEventIdAndEstimatorId(Long eventId, Long estimatorId);
}
