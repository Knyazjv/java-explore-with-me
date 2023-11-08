package ru.practicum.evmmainservice.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDto {
    private Long id;

    @Length(max = 50)
    @NotBlank
    private String name;
}
