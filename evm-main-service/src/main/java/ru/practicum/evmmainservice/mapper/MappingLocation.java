package ru.practicum.evmmainservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.evmmainservice.dto.LocationDto;
import ru.practicum.evmmainservice.entity.Location;

@Component
public class MappingLocation {
    public LocationDto toDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }

    public Location toLocation(LocationDto locationDto) {
        return new Location(null, locationDto.getLat(), locationDto.getLon());
    }

    public Location toLocationWithId(Long id, LocationDto locationDto) {
        return new Location(id, locationDto.getLat(), locationDto.getLon());
    }
}
