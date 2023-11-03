package ru.practicum.evmmainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmmainservice.entity.Category;

public interface EvmCategoryRepository extends JpaRepository<Category, Long> {
}
