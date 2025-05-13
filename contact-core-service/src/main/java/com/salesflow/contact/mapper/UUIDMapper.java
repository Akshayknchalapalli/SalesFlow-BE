package com.salesflow.contact.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import java.util.UUID;

/**
 * Mapper for handling UUID conversions.
 * This mapper is used by other mappers to handle UUID fields.
 */
@Mapper(componentModel = "spring")
public interface UUIDMapper {
    
    /**
     * Maps a UUID value, handling null cases.
     *
     * @param uuid the UUID to map
     * @return the mapped UUID, or null if input is null
     */
    @Named("mapUUID")
    default UUID map(UUID uuid) {
        return uuid;
    }

    /**
     * Maps a String UUID to UUID object.
     *
     * @param uuidString the UUID string to map
     * @return the mapped UUID, or null if input is null
     */
    @Named("mapStringToUUID")
    default UUID mapStringToUUID(String uuidString) {
        return uuidString == null ? null : UUID.fromString(uuidString);
    }

    /**
     * Maps a UUID to String.
     *
     * @param uuid the UUID to map
     * @return the string representation of UUID, or null if input is null
     */
    @Named("mapUUIDToString")
    default String mapUUIDToString(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }
} 