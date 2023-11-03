package ru.practicum.evmmainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDto {
    private Long id;

    @NotBlank
    private String name;
}
