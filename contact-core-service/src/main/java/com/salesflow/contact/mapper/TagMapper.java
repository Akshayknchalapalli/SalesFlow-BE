package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.Tag;
import com.salesflow.contact.dto.TagDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InheritConfiguration;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TagMapper {
    
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "colorHex", source = "colorHex")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    TagDTO toDTO(Tag entity);
    
    @InheritInverseConfiguration(name = "toDTO")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Tag toEntity(TagDTO dto);
    
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDTO(TagDTO dto, @MappingTarget Tag entity);
} 