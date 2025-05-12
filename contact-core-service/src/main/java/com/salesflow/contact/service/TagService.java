package com.salesflow.contact.service;

import com.salesflow.contact.dto.TagDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TagService {
    
    TagDTO createTag(TagDTO tagDTO, String ownerId);
    
    TagDTO updateTag(UUID id, TagDTO tagDTO, String ownerId);
    
    void deleteTag(UUID id, String ownerId);
    
    TagDTO getTag(UUID id, String ownerId);
    
    Page<TagDTO> getTags(String ownerId, Pageable pageable);
    
    Page<TagDTO> searchTags(String ownerId, String searchTerm, Pageable pageable);
    
    List<TagDTO> getAllTags(String ownerId);
    
    void addTagToContact(UUID contactId, UUID tagId, String ownerId);
    
    void removeTagFromContact(UUID contactId, UUID tagId, String ownerId);
    
    List<TagDTO> getContactTags(UUID contactId, String ownerId);
} 