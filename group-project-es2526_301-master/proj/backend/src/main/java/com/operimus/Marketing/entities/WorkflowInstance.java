package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Entity
@Data
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Lead lead;  // the lead being tracked

    @ManyToOne
    private Workflow workflow; // the workflow blueprint

    @ManyToMany
    @JoinTable(
         name = "workflow_instance_active_nodes",
         joinColumns = @JoinColumn(name = "workflow_instance_id"),
         inverseJoinColumns = @JoinColumn(name = "node_id")
    ) 
    private Set<Node> activeNodes = new HashSet<>();

    @ElementCollection
    private Set<Long> completedNodeIds = new HashSet<>(); // executed nodes

    @Column(nullable = false)
    private boolean finished = false; // indicates workflow instance has fully completed
}