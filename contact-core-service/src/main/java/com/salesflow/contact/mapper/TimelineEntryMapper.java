package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.TimelineEntry;
import com.salesflow.contact.dto.TimelineEntryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InheritConfiguration;

@Mapper(componentModel = "spring",
        uses = {UUIDMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TimelineEntryMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "contactId", source = "contact.id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    TimelineEntryDTO toDTO(TimelineEntry entity);
    
    @InheritInverseConfiguration(name = "toDTO")
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    TimelineEntry toEntity(TimelineEntryDTO dto);
    
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDTO(TimelineEntryDTO dto, @MappingTarget TimelineEntry entity);
} 