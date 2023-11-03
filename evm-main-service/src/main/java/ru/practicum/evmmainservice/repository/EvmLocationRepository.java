package ru.practicum.evmmainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmmainservice.entity.Location;

public interface EvmLocationRepository extends JpaRepository<Location, Long> {
}
