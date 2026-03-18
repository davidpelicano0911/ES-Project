package com.operimus.Marketing.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "campaign", uniqueConstraints = @UniqueConstraint(name = "uk_campaign_name", columnNames = "name"))
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToMany
    @JoinTable(
        name = "campaign_segments",
        joinColumns = @JoinColumn(name = "campaign_id"),
        inverseJoinColumns = @JoinColumn(name = "segment_id")
    )
    private Set<Segments> segment = new HashSet<>();

    private Date createdAt;

    private Date dueDate;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "workflow_id")
    @Nullable
    private Workflow workflow;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dashboard_id")
    @JsonManagedReference
    @Nullable
    private Dashboard dashboard;

    @ManyToMany(mappedBy = "campaigns")
    @JsonIgnoreProperties({
        "campaigns", "workflow", "dashboard", 
        "hibernateLazyInitializer", "handler"
    })
    private Set<CampaignMaterials> materials = new HashSet<>();


    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore 
    private List<CampaignReport> reports = new ArrayList<>();

    public List<CampaignReport> getReports() {
        return reports;
    }

    public void setReports(List<CampaignReport> reports) {
        this.reports = reports;
    }

    public Campaign() {
    }

    public Campaign(String name, String description, Set<Segments> segment, Date createdAt, Date dueDate, CampaignStatus status, Workflow workflow) {
        this.name = name;
        this.description = description;
        this.segment = segment;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.workflow = workflow;
        this.status = status;
    }

    public Campaign(String name, String description, Set<Segments> segment, Date createdAt, Date dueDate, CampaignStatus status, Dashboard dashboard) {
        this.name = name;
        this.description = description;
        this.segment = segment;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.dashboard = dashboard;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Segments> getSegment() {
        return segment;
    }

    public void setSegment(Set<Segments> segment) {
        this.segment = segment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public Set<CampaignMaterials> getMaterials() {
        return materials;
    }

    public void setMaterials(Set<CampaignMaterials> materials) {
        this.materials = materials;
    }
}
