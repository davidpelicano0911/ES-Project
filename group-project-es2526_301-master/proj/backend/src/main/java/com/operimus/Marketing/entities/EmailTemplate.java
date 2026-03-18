package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import lombok.EqualsAndHashCode;


import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("EMAIL")
public class EmailTemplate extends CampaignMaterials {

    private String subject;

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    private String body;

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String design;

    @JsonIgnore
    private String created_by;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    public Object map(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'map'");
    }
}