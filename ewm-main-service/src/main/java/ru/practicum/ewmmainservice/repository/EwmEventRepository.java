package ru.practicum.ewmmainservice.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewmmainservice.entity.Event;
import ru.practicum.ewmmainservice.enumEwm.State;

import java.util.List;
import java.util.Optional;

public interface EwmEventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    List<Event> findAllByInitiatorId(Long userId, PageRequest pageRequest);

    List<Event> findAllByIdIn(List<Long> ids);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    List<Event> findAllByCategory_Id(Long catId);

    Optional<Event> findByIdAndState(Long eventId, State state);
}
