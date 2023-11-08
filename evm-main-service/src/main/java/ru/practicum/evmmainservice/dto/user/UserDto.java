package ru.practicum.evmmainservice.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private Long id;

    @Length(min = 2, max = 250)
    @NotBlank
    private String name;

    @Length(min = 6, max = 254)
    @NotBlank
    @Email
    private String email;
}
