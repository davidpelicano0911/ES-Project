package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "landing_page_event")
public class LandingPageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "landing_page_id")
    private Long landingPageId;

    @Column(name = "lead_id")
    private Long leadId;


    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    // store metadata as JSON text
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
