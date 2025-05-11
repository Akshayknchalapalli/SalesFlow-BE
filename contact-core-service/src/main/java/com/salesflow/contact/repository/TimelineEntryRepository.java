package com.salesflow.contact.repository;

import com.salesflow.contact.domain.TimelineEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineEntryRepository extends JpaRepository<TimelineEntry, Long> {
    
    Page<TimelineEntry> findByContactId(Long contactId, Pageable pageable);
    
    List<TimelineEntry> findByContactIdOrderByCreatedAtDesc(Long contactId);
    
    @Query("SELECT te FROM TimelineEntry te WHERE te.contactId = :contactId AND " +
           "te.type = :type ORDER BY te.createdAt DESC")
    List<TimelineEntry> findByContactIdAndTypeOrderByCreatedAtDesc(
        @Param("contactId") Long contactId,
        @Param("type") TimelineEntry.EntryType type
    );
    
    @Query("SELECT COUNT(te) FROM TimelineEntry te WHERE te.contactId = :contactId AND " +
           "te.type = :type")
    long countByContactIdAndType(
        @Param("contactId") Long contactId,
        @Param("type") TimelineEntry.EntryType type
    );
} 