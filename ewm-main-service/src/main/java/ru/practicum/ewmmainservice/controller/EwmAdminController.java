package ru.practicum.ewmmainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmmainservice.dto.category.CategoryDto;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoRequest;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventUpdateDtoRequest;
import ru.practicum.ewmmainservice.dto.event.GetEventRequestAdmin;
import ru.practicum.ewmmainservice.dto.user.UserDto;
import ru.practicum.ewmmainservice.dto.validateInterface.Create;
import ru.practicum.ewmmainservice.dto.validateInterface.Update;
import ru.practicum.ewmmainservice.service.EwmAdminService;

import javax.validation.Valid;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(value = "/admin")
@RequiredArgsConstructor
public class EwmAdminController {

    private final EwmAdminService ewmAdminService;

    @PostMapping(value = "/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /admin/users");
        return ResponseEntity.status(HttpStatus.CREATED).body(ewmAdminService.createUser(userDto));
    }

    @DeleteMapping(value = "/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        ewmAdminService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(value = "/users")
    public ResponseEntity<List<UserDto>> getUsers(@RequestParam(required = false) List<Long> ids,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /admin/users/{}", ids);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ewmAdminService.getUsersWithPagination(ids, PageRequest.of(from / size, size)));
    }

    @PostMapping(value = "/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("POST /admin/categories");
        return ResponseEntity.status(HttpStatus.CREATED).body(ewmAdminService.createCategory(categoryDto));
    }

    @DeleteMapping(value = "/categories/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{}", catId);
        ewmAdminService.deleteCategory(catId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(value = "/categories/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto,
                                                       @PathVariable Long catId) {
        log.info("PATCH /admin/categories/{}", catId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmAdminService.updateCategory(catId, categoryDto));
    }

    @PostMapping(value = "/compilations")
    public ResponseEntity<CompilationDtoResponse> createCompilation(@Validated({Create.class})
                                                                        @RequestBody CompilationDtoRequest cdr) {
        log.info("POST /admin/compilations");
        return ResponseEntity.status(HttpStatus.CREATED).body(ewmAdminService.createCompilation(cdr));
    }


    @DeleteMapping(value = "/compilations/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{}", compId);
        ewmAdminService.deleteCompilation(compId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(value = "/compilations/{compId}")
    public ResponseEntity<CompilationDtoResponse> updateCompilation(@Validated({Update.class})
                                                                        @RequestBody CompilationDtoRequest cdr,
                                                                    @PathVariable Long compId) {
        log.info("PATCH /admin/compilations/{}", compId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmAdminService.updateCompilation(cdr, compId));
    }

    @PatchMapping(value = "/events/{eventId}")
    public ResponseEntity<EventDtoResponse> updateEventById(@PathVariable Long eventId,
                                                            @Valid @RequestBody EventUpdateDtoRequest eventDtoRequest) {
        log.info("PATCH /admin/events/{}", eventId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmAdminService.updateEventById(eventId, eventDtoRequest));
    }

    @GetMapping(value = "/events")
    public ResponseEntity<List<EventDtoResponse>> getEventsAdmin(@RequestParam(required = false) List<Long> users,
                                                      @RequestParam(required = false) List<String> states,
                                                      @RequestParam(required = false) List<Long> categories,
                                                      @RequestParam(required = false) String rangeStart,
                                                      @RequestParam(required = false) String rangeEnd,
                                                      @RequestParam(defaultValue = "0") Integer from,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /admin/events");
        return ResponseEntity.status(HttpStatus.OK)
                .body(ewmAdminService.getEvents(GetEventRequestAdmin.of(users,
                        states,
                        categories,
                        rangeStart,
                        rangeEnd),
                        from,
                        size));
    }
}
