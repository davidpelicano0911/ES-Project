package com.operimus.Marketing.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Date;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@Table(name = "lead", uniqueConstraints = @UniqueConstraint(name = "uk_lead_email", columnNames = "email"))
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String country;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    private Date createdAt;

    private Boolean isSubscribed;
    
    // Lead score (computed based on interactions). Stored as integer percentage-like value (0-100+)
    private Integer score;

    @Column(nullable = true)
    private String crmId;

    private Date lastUpdatedAt;

    private Date lastSyncedAt;

    private List<Long> segmentIds;

    private String status;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        score = calculateScore();
        calculateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = new Date();
        score = calculateScore();
        calculateStatus();
    }

    //Calculate score based on first name, last name, country, email and phone number
    public int calculateScore() {
        int calculatedScore = 0;
        if (firstName != null && !firstName.isEmpty()) {
            calculatedScore += 20;
        }
        if (lastName != null && !lastName.isEmpty()) {
            calculatedScore += 20;
        }
        if (country != null && !country.isEmpty()) {
            calculatedScore += 20;
        }
        if (email != null && !email.isEmpty()) {
            calculatedScore += 20;
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            calculatedScore += 20;
        }
        return calculatedScore;
    }

    public void calculateStatus() {
        if (score == null) {
            status = "Cold";
        } else if (score >= 67) {
            status = "Hot";
        } else if (score >= 34) {
            status = "Warm";
        } else {
            status = "Cold";
        }
    }

    public void addSegment(Long segmentId) {
        if (segmentIds == null) {
            segmentIds = new ArrayList<>();
        }
        if (!segmentIds.contains(segmentId)) {
            segmentIds.add(segmentId);
        }
    }

    public void removeSegment(Long segmentId) {
        if (segmentIds != null) {
            segmentIds.remove(segmentId);
        }
    }

    public List<Long> getSegmentIds() {
        if (segmentIds == null) {
            segmentIds = new ArrayList<>();
        }
        return segmentIds;
    }


}
