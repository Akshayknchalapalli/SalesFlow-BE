package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.TimelineEntry;
import com.salesflow.contact.dto.TimelineEntryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TimelineEntryMapper {
    
    @Mapping(target = "contactId", source = "contact.id")
    @Mapping(target = "type", source = "type")
    TimelineEntryDTO toDTO(TimelineEntry entity);
    
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "type", source = "type")
    TimelineEntry toEntity(TimelineEntryDTO dto);
    
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "type", source = "type")
    void updateEntityFromDTO(TimelineEntryDTO dto, @MappingTarget TimelineEntry entity);
} 