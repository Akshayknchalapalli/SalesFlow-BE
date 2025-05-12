package com.salesflow.contact.repository;

import com.salesflow.contact.domain.TimelineEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimelineEntryRepository extends JpaRepository<TimelineEntry, UUID> {
    
    Page<TimelineEntry> findByContactId(UUID contactId, Pageable pageable);
    
    List<TimelineEntry> findByContactIdOrderByCreatedAtDesc(UUID contactId);
    
    @Query("SELECT te FROM TimelineEntry te WHERE te.contact.id = :contactId AND " +
           "te.type = :type ORDER BY te.createdAt DESC")
    List<TimelineEntry> findByContactIdAndTypeOrderByCreatedAtDesc(
        @Param("contactId") UUID contactId,
        @Param("type") TimelineEntry.EntryType type
    );
    
    @Query("SELECT COUNT(te) FROM TimelineEntry te WHERE te.contact.id = :contactId AND " +
           "te.type = :type")
    long countByContactIdAndType(
        @Param("contactId") UUID contactId,
        @Param("type") TimelineEntry.EntryType type
    );
} 