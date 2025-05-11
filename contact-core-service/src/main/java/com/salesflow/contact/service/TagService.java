package com.salesflow.contact.service;

import com.salesflow.contact.dto.TagDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {
    
    TagDTO createTag(TagDTO tagDTO, String ownerId);
    
    TagDTO updateTag(Long id, TagDTO tagDTO, String ownerId);
    
    void deleteTag(Long id, String ownerId);
    
    TagDTO getTag(Long id, String ownerId);
    
    Page<TagDTO> getTags(String ownerId, Pageable pageable);
    
    Page<TagDTO> searchTags(String ownerId, String searchTerm, Pageable pageable);
    
    List<TagDTO> getAllTags(String ownerId);
    
    void addTagToContact(Long contactId, Long tagId, String ownerId);
    
    void removeTagFromContact(Long contactId, Long tagId, String ownerId);
    
    List<TagDTO> getContactTags(Long contactId, String ownerId);
} 