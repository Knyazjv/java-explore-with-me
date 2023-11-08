package ru.practicum.evmmainservice.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import ru.practicum.evmmainservice.dto.validateInterface.Create;
import ru.practicum.evmmainservice.dto.validateInterface.Update;

import javax.validation.constraints.NotBlank;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompilationDtoRequest {

    private List<Long> events;

    private Boolean pinned;

    @Length(max = 50,groups = {Update.class, Create.class})
    @NotBlank(groups = {Create.class})
    private String title;
}