package com.salesflow.activity.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "contact_id", nullable = false)
    private UUID contactId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ActivityType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "priority")
    private String priority;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status.equals("COMPLETED") && completedTime == null) {
            completedTime = LocalDateTime.now();
        }
    }
} 