package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.ContactPreferences;
import com.salesflow.contact.dto.ContactPreferencesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactPreferencesMapper {
    ContactPreferences toEntity(ContactPreferencesDTO dto);
    ContactPreferencesDTO toDTO(ContactPreferences entity);
} 