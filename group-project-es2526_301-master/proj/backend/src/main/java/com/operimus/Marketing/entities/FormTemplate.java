package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

// @Table(name = "form_template", uniqueConstraints = @UniqueConstraint(name = "uk_form_template_name", columnNames = "name"))
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@DiscriminatorValue("FORM")
public class FormTemplate extends CampaignMaterials {

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "form_json", columnDefinition = "TEXT")
    private String formJson;

    public FormTemplate() {}
    public FormTemplate(String name, String description, Long createdBy, String formJson) {
        super(); 
        this.setName(name);
        this.setDescription(description);
        this.createdBy = createdBy;
        this.formJson = formJson;
        this.isPublished = false;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}