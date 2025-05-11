package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.SocialProfile;
import com.salesflow.contact.dto.SocialProfileDTO;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface SocialProfileMapper {
    SocialProfile toEntity(SocialProfileDTO dto);
    SocialProfileDTO toDTO(SocialProfile entity);
} 