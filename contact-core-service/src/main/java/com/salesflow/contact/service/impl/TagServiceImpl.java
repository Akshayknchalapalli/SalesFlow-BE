package com.salesflow.contact.service.impl;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.domain.Tag;
import com.salesflow.contact.dto.TagDTO;
import com.salesflow.contact.exception.ContactNotFoundException;
import com.salesflow.contact.exception.DuplicateTagNameException;
import com.salesflow.contact.exception.TagNotFoundException;
import com.salesflow.contact.mapper.TagMapper;
import com.salesflow.contact.repository.ContactRepository;
import com.salesflow.contact.repository.TagRepository;
import com.salesflow.contact.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final ContactRepository contactRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagDTO createTag(TagDTO tagDTO, String ownerId) {
        if (tagRepository.existsByNameAndIdNot(tagDTO.getName(), null)) {
            throw new DuplicateTagNameException("Tag name already exists: " + tagDTO.getName());
        }

        Tag tag = tagMapper.toEntity(tagDTO);
        tag.setOwnerId(ownerId);
        tag.setCreatedBy(ownerId);
        tag.setUpdatedBy(ownerId);
        tag = tagRepository.save(tag);
        return tagMapper.toDTO(tag);
    }

    @Override
    @Transactional
    public TagDTO updateTag(Long id, TagDTO tagDTO, String ownerId) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));

        if (!tag.getOwnerId().equals(ownerId)) {
            throw new TagNotFoundException("Tag not found with id: " + id);
        }

        if (tagRepository.existsByNameAndIdNot(tagDTO.getName(), id)) {
            throw new DuplicateTagNameException("Tag name already exists: " + tagDTO.getName());
        }

        tagMapper.updateEntityFromDTO(tagDTO, tag);
        tag = tagRepository.save(tag);
        return tagMapper.toDTO(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id, String ownerId) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));

        if (!tag.getOwnerId().equals(ownerId)) {
            throw new TagNotFoundException("Tag not found with id: " + id);
        }

        tagRepository.delete(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getTag(Long id, String ownerId) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));

        if (!tag.getOwnerId().equals(ownerId)) {
            throw new TagNotFoundException("Tag not found with id: " + id);
        }

        return tagMapper.toDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> getTags(String ownerId, Pageable pageable) {
        return tagRepository.findByOwnerId(ownerId, pageable)
                .map(tagMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDTO> searchTags(String ownerId, String searchTerm, Pageable pageable) {
        return tagRepository.searchTags(ownerId, searchTerm, pageable)
                .map(tagMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags(String ownerId) {
        return tagRepository.findByOwnerId(ownerId)
                .stream()
                .map(tagMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void addTagToContact(Long contactId, Long tagId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + tagId));

        if (!tag.getOwnerId().equals(ownerId)) {
            throw new TagNotFoundException("Tag not found with id: " + tagId);
        }

        contact.addTag(tag);
        contactRepository.save(contact);
    }

    @Override
    @Transactional
    public void removeTagFromContact(Long contactId, Long tagId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + tagId));

        if (!tag.getOwnerId().equals(ownerId)) {
            throw new TagNotFoundException("Tag not found with id: " + tagId);
        }

        contact.removeTag(tag);
        contactRepository.save(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getContactTags(Long contactId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        return contact.getTags()
                .stream()
                .map(tagMapper::toDTO)
                .toList();
    }
} 