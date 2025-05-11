package com.salesflow.contact.mapper;

import org.mapstruct.Mapper;

import com.salesflow.contact.domain.Note;
import com.salesflow.contact.dto.NoteDTO;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    NoteDTO toDTO(Note note);

    Note toEntity(NoteDTO noteDTO);
}
