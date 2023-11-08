package ru.practicum.evmmainservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.evmmainservice.dto.category.CategoryDto;
import ru.practicum.evmmainservice.entity.Category;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MappingCategory {
    public CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public Category toCategory(CategoryDto categoryDto) {
        return new Category(null, categoryDto.getName());
    }

    public List<CategoryDto> toCategoryDtos(List<Category> categories) {
        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
