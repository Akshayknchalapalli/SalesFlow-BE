package com.salesflow.contact.mapper;

import org.mapstruct.Mapper;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UUIDMapper {
    default UUID map(UUID uuid) {
        return uuid;
    }
} 