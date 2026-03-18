package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "form_submission", uniqueConstraints = @UniqueConstraint(name = "uk_form_submission_id", columnNames = "id"))
public class FormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "submitted_at", updatable = false)
    private Date submittedAt;

    // store responses JSON as TEXT
    @Lob
    @Column(name = "responses_json", columnDefinition = "TEXT")
    private String responsesJson;

    @PrePersist
    protected void onCreate() {
        submittedAt = new Date();
    }
}
