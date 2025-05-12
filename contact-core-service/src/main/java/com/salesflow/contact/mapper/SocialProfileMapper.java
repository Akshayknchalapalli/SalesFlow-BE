package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.SocialProfile;
import com.salesflow.contact.dto.SocialProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SocialProfileMapper {
    
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "profileUrl", source = "profileUrl")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "verified", source = "verified")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    SocialProfile toEntity(SocialProfileDTO dto);
    
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "profileUrl", source = "profileUrl")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "verified", source = "verified")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    SocialProfileDTO toDTO(SocialProfile entity);
} 