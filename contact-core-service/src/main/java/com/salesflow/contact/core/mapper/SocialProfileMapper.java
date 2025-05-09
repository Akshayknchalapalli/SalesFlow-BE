package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.SocialProfile;
import com.salesflow.contact.core.dto.SocialProfileDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SocialProfileMapper {
    SocialProfile toEntity(SocialProfileDTO dto);
    SocialProfileDTO toDTO(SocialProfile entity);
} 