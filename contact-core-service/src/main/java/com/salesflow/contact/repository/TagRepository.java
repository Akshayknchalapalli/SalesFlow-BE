package com.salesflow.contact.repository;

import com.salesflow.contact.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    
    Optional<Tag> findByName(String name);
    
    Page<Tag> findByOwnerId(String ownerId, Pageable pageable);
    
    @Query("SELECT t FROM Tag t WHERE t.ownerId = :ownerId AND " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tag> searchTags(@Param("ownerId") String ownerId,
                        @Param("searchTerm") String searchTerm,
                        Pageable pageable);
    
    List<Tag> findByOwnerId(String ownerId);
    
    boolean existsByNameAndIdNot(String name, UUID id);
} 