package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.Tag;
import com.salesflow.contact.dto.TagDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TagMapper {
    Tag toEntity(TagDTO dto);
    TagDTO toDTO(Tag entity);
    void updateEntityFromDTO(TagDTO dto, @MappingTarget Tag entity);
} 