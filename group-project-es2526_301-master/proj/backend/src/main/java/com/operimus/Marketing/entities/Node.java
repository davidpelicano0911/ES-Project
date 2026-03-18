package com.operimus.Marketing.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.operimus.Marketing.dto.EventDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.entities.OnFormSubmittedTrigger;
import com.operimus.Marketing.entities.AddToSegmentAction;
import com.operimus.Marketing.entities.DelayAction;
import com.operimus.Marketing.entities.RemoveFromSegmentAction;
import com.operimus.Marketing.entities.SendEmailAction;
import com.operimus.Marketing.entities.WorkflowInstance;


@Entity
@Data
@ToString(exclude = {"workflow", "outgoingNodes"})
@EqualsAndHashCode(exclude = {"workflow", "outgoingNodes"})
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "nodeType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Node.class, name = "START"),
        @JsonSubTypes.Type(value = Node.class, name = "END"),
        @JsonSubTypes.Type(value = OnFormSubmittedTrigger.class, name = "FORM"),
        @JsonSubTypes.Type(value = AddToSegmentAction.class, name = "ADD_TO_SEGMENT"),
        @JsonSubTypes.Type(value = RemoveFromSegmentAction.class, name = "REMOVE_FROM_SEGMENT"),
        @JsonSubTypes.Type(value = SendEmailAction.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = DelayAction.class, name = "DELAY"),
        // add other node types as needed
})
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer positionX;
    private Integer positionY;

    private Boolean isStartNode;
    private Boolean isEndNode;

    private Integer score;

    private String label;


    @Enumerated(EnumType.STRING)
    private NodeType nodeType;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    @JsonBackReference
    private Workflow workflow;

    @ManyToMany
    @JoinTable(
        name = "node_connections",
        joinColumns = @JoinColumn(name = "from_node_id"),
        inverseJoinColumns = @JoinColumn(name = "to_node_id")
    )
    private Set<Node> outgoingNodes = new HashSet<>();

    
    public void addOutgoingNode(Node target) {
        if (target == null || target.equals(this) || outgoingNodes.contains(target)) {
            throw new IllegalArgumentException("Invalid target node");
        }

        if (createsCycle(target)) {
            throw new IllegalArgumentException("Adding this edge would create a cycle");
        }

        outgoingNodes.add(target);
    }

    private boolean createsCycle(Node target) {
        return isReachable(target, this);
    }

    private boolean isReachable(Node start, Node goal) {
        Set<Node> visited = new HashSet<>();
        return dfs(start, goal, visited);
    }

    private boolean dfs(Node current, Node goal, Set<Node> visited) {
        if (current.equals(goal)) return true;
        if (!visited.add(current)) return false;

        for (Node next : current.getOutgoingNodes()) {
            if (dfs(next, goal, visited)) return true;
        }
        return false;
    }

    public void removeOutgoingNode(Node target) {
        outgoingNodes.remove(target);
    }

    @Override
    public String toString() {
        return "Node{id=" + id + ", type='" + nodeType + "'}";
    }


    public boolean matchesEvent(EventDTO event) {
        System.out.println("Matching event in Node base class - should be overridden");
        return false;
    }


    public WorkflowInstance executeNodeLogic(WorkflowInstance instance) {
        instance.getActiveNodes().remove(this);
        instance.getCompletedNodeIds().add(this.getId());

        for (Node nextNode : outgoingNodes) {
            System.out.println("Processing next node: " + nextNode.getId());
            System.out.println(" Next node type: " + nextNode.getNodeType());
            if (!instance.getCompletedNodeIds().contains(nextNode.getId())) {
                instance.getActiveNodes().add(nextNode);
            }
        }
        return instance;
    }
} 