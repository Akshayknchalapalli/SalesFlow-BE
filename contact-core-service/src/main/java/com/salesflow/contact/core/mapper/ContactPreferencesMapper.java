package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.ContactPreferences;
import com.salesflow.contact.core.dto.ContactPreferencesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactPreferencesMapper {
    ContactPreferences toEntity(ContactPreferencesDTO dto);
    ContactPreferencesDTO toDTO(ContactPreferences entity);
} 