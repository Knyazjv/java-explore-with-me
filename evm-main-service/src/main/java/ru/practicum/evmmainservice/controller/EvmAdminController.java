package ru.practicum.evmmainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmmainservice.dto.*;
import ru.practicum.evmmainservice.service.EvmAdminService;

import javax.validation.Valid;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(value = "/admin")
@RequiredArgsConstructor
public class EvmAdminController {

    private final EvmAdminService evmAdminService;

    @PostMapping(value = "/users")
    public ResponseEntity<UserDto> createUser(@Validated({Create.class}) @RequestBody UserDto userDto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(evmAdminService.createUser(userDto));
    }

    @DeleteMapping(value = "/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        evmAdminService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(value = "/users")
    public ResponseEntity<List<UserDto>> getUsers(@RequestParam List<Long> ids,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(evmAdminService.getUsersWithPagination(ids, PageRequest.of(from / size, size)));
    }

    @PostMapping(value = "/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evmAdminService.createCategory(categoryDto));
    }

    @DeleteMapping(value = "/categories/{catId}")
    private ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        evmAdminService.deleteCategory(catId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(value = "/categories/{catId}")
    private ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto,
                                                       @PathVariable Long catId) {
        return ResponseEntity.status(HttpStatus.OK).body(evmAdminService.updateCategory(catId, categoryDto));
    }

    @PostMapping(value = "/compilations")
    public ResponseEntity<CompilationDtoResponse> createCompilation(@Valid @RequestBody CompilationDtoRequest cdr) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evmAdminService.createCompilation(cdr));
    }

    @DeleteMapping(value = "/compilations/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        evmAdminService.deleteCompilation(compId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(value = "/compilations/{compId}")
    public ResponseEntity<CompilationDtoResponse> updateCompilation(@RequestBody CompilationDtoRequest cdr,
                                                                    @PathVariable Long compId) {
        return ResponseEntity.status(HttpStatus.OK).body(evmAdminService.updateCompilation(cdr, compId));
    }

    @PatchMapping(value = "/events/{eventId}")
    public ResponseEntity<EventDtoResponse> updateEventById(@PathVariable Long eventId,
                                                            @RequestBody EventUpdateDtoRequest eventDtoRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(evmAdminService.updateEventById(eventId, eventDtoRequest));
    }

    @GetMapping(value = "/events")
    public ResponseEntity<List<EventDtoResponse>> getEventsAdmin(@RequestParam(required = false) List<Long> users,
                                                      @RequestParam(required = false) List<String> states,
                                                      @RequestParam(required = false) List<Long> categories,
                                                      @RequestParam(required = false) String rangeStart,
                                                      @RequestParam(required = false) String rangeEnd,
                                                      @RequestParam(defaultValue = "0") Integer from,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmAdminService.getEvents(GetEventRequestAdmin.of(users,
                        states,
                        categories,
                        rangeStart,
                        rangeEnd),
                        from,
                        size));
    }
}
