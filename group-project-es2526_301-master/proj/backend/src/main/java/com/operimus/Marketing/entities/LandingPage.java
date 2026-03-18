package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;


@Entity
@Data
@EqualsAndHashCode(callSuper = true)  
@DiscriminatorValue("LANDING_PAGE")  
public class LandingPage extends CampaignMaterials {

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    private String body;

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    private String design;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}