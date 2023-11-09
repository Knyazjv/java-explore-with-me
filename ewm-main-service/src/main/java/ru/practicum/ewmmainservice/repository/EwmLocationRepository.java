package ru.practicum.ewmmainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmainservice.entity.Location;

public interface EwmLocationRepository extends JpaRepository<Location, Long> {
}
