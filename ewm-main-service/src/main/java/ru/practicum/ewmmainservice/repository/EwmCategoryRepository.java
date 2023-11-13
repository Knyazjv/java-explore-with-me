package ru.practicum.ewmmainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmainservice.entity.Category;

public interface EwmCategoryRepository extends JpaRepository<Category, Long> {
}
