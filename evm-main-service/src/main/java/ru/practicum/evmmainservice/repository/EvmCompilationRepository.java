package ru.practicum.evmmainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmmainservice.entity.Compilation;

public interface EvmCompilationRepository extends JpaRepository<Compilation, Long> {
}
