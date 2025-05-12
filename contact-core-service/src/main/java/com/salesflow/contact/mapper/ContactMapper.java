package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.dto.ContactDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {ContactPreferencesMapper.class, AddressMapper.class, SocialProfileMapper.class, 
                NoteMapper.class, TagMapper.class, TimelineEntryMapper.class, UUIDMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "jobTitle", source = "jobTitle")
    @Mapping(target = "stage", source = "stage")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "teamId", source = "teamId")
    @Mapping(target = "regionId", source = "regionId")
    @Mapping(target = "preferences", source = "preferences")
    @Mapping(target = "addresses", source = "addresses")
    @Mapping(target = "socialProfiles", source = "socialProfiles")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "timelineEntries", source = "timelineEntries")
    @Mapping(target = "relatedContacts", source = "relatedContacts")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    ContactDTO toDTO(Contact entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "relatedContacts", ignore = true)
    Contact toEntity(ContactDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "relatedContacts", ignore = true)
    void updateEntityFromDTO(ContactDTO dto, @MappingTarget Contact entity);
} 