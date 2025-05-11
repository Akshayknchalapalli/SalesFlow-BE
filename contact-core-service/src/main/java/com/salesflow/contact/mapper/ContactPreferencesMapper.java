package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.ContactPreferences;
import com.salesflow.contact.dto.ContactPreferencesDTO;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface ContactPreferencesMapper {
    ContactPreferences toEntity(ContactPreferencesDTO dto);
    ContactPreferencesDTO toDTO(ContactPreferences entity);
} 