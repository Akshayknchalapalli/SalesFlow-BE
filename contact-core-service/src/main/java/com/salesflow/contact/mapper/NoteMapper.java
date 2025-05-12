package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.Note;
import com.salesflow.contact.dto.NoteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NoteMapper {
    @Mapping(target = "contactId", source = "contact.id")
    NoteDTO toDTO(Note entity);

    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Note toEntity(NoteDTO dto);

    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDTO(NoteDTO dto, @MappingTarget Note entity);
}
