package com.operimus.Marketing.entities;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString; 

@Entity
@Data
@ToString(exclude = {"nodes", "campaign"})
@EqualsAndHashCode(exclude = {"nodes", "campaign"})
@Table(name = "workflow", uniqueConstraints = @UniqueConstraint(name = "uk_workflow_name", columnNames = "name"))
public class Workflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @Column(nullable = false)
    private Boolean isReadyToUse = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "workflow_id") 
    @JsonManagedReference
    private List<Node> nodes = new ArrayList<>();

    @OneToOne(mappedBy = "workflow")
    @Nullable
    @JsonIgnore
    private Campaign campaign;


    public void addNode(Node node) {
        nodes.add(node);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = new Date();
    }

    /**
     * Calculate workflow readiness based on node completeness and chain connectivity.
     * Workflow is ready if:
     * 1. All nodes have required attributes filled
     * 2. All nodes are connected in a chain (no disconnected nodes)
     * 3. There is at least one START node
     */
    public void updateStatus() {
        if (nodes == null || nodes.isEmpty()) {
            this.isReadyToUse = false;
            return;
        }

        // Find START node
        Node startNode = null;
        for (Node node : nodes) {
            if (Boolean.TRUE.equals(node.getIsStartNode())) {
                startNode = node;
                break;
            }
        }

        if (startNode == null) {
            this.isReadyToUse = false;
            return;
        }

        // Check all nodes have required attributes
        for (Node node : nodes) {
            if (!isNodeComplete(node)) {
                this.isReadyToUse = false;
                return;
            }
        }

        // Check all nodes are reachable from START (part of the chain)
        Set<Long> reachableNodes = new HashSet<>();
        traverseNodes(startNode, reachableNodes);

        // If not all nodes are reachable, there are disconnected nodes
        if (reachableNodes.size() != nodes.size()) {
            this.isReadyToUse = false;
            return;
        }

        this.isReadyToUse = true;
    }

    /**
     * Recursively traverse nodes from a starting point to find all reachable nodes.
     */
    private void traverseNodes(Node node, Set<Long> visited) {
        if (node == null || node.getId() == null || visited.contains(node.getId())) {
            return;
        }

        visited.add(node.getId());

        if (node.getOutgoingNodes() != null) {
            for (Node outgoing : node.getOutgoingNodes()) {
                traverseNodes(outgoing, visited);
            }
        }
    }

    private boolean isNodeComplete(Node node) {
        if (node.getNodeType() == null) {
            return false;
        }

        // Check type-specific required attributes
        if (node instanceof SendEmailAction) {
            SendEmailAction emailAction = (SendEmailAction) node;
            return emailAction.getEmailTemplateId() != null && emailAction.getSendFrom() != null;
        } else if (node instanceof OnFormSubmittedTrigger) {
            OnFormSubmittedTrigger formTrigger = (OnFormSubmittedTrigger) node;
            return formTrigger.getFormId() != null;
        } else if (node instanceof AddToSegmentAction) {
            AddToSegmentAction segmentAction = (AddToSegmentAction) node;
            return segmentAction.getSegmentId() != null;
        } else if (node instanceof RemoveFromSegmentAction) {
            RemoveFromSegmentAction segmentAction = (RemoveFromSegmentAction) node;
            return segmentAction.getSegmentId() != null;
        } else if (node instanceof DelayAction) {
            DelayAction delayAction = (DelayAction) node;
            return delayAction.getDelayDuration() != null && delayAction.getDelayUnit() != null;
        }

        // START and END nodes are always complete if they have nodeType
        return true;
    }

    @Override
    public String toString() {
        return "Workflow{id=" + id + ", name='" + name + "'}";
    }
}
