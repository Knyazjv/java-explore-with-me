package ru.practicum.ewmmainservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewmmainservice.dto.location.LocationDto;
import ru.practicum.ewmmainservice.entity.Location;

@Component
public class MappingLocation {
    public LocationDto toDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }

    public Location toLocation(LocationDto locationDto) {
        return new Location(null, locationDto.getLat(), locationDto.getLon());
    }
}
