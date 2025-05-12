package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.SocialProfile;
import com.salesflow.contact.dto.SocialProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SocialProfileMapper {
    SocialProfile toEntity(SocialProfileDTO dto);
    SocialProfileDTO toDTO(SocialProfile entity);
} 