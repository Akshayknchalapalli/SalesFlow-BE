package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.Note;
import com.salesflow.contact.dto.NoteDTO;
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
public interface NoteMapper {
    
    @Mapping(target = "content", source = "content")
    @Mapping(target = "contactId", source = "contact.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    NoteDTO toDTO(Note entity);

    @InheritInverseConfiguration(name = "toDTO")
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Note toEntity(NoteDTO dto);

    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDTO(NoteDTO dto, @MappingTarget Note entity);
}
