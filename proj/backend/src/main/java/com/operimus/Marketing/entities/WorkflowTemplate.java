package com.operimus.Marketing.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Temporal;

import java.util.Date;

@Entity
@Data
@Table(name = "workflow_template", uniqueConstraints = @UniqueConstraint(name = "uk_workflow_template_name", columnNames = "name"))
public class WorkflowTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String templateData;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
