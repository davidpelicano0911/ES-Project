package com.operimus.Marketing.services;

import com.operimus.Marketing.dto.EventDTO;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.entities.NodeType;
import com.operimus.Marketing.entities.WorkflowInstance;
import com.operimus.Marketing.repositories.LeadRepository;
import com.operimus.Marketing.repositories.NodeRepository;
import com.operimus.Marketing.repositories.WorkflowInstanceRepository;
import jakarta.annotation.PostConstruct;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.operimus.Marketing.entities.OnFormSubmittedTrigger;
import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.repositories.WorkflowRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.CampaignStatus;
import com.operimus.Marketing.entities.EventType;
import com.operimus.Marketing.repositories.CampaignRepository;

@Service
public class WorkflowEngine {

    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private LeadRepository leadRepository;

    @Autowired  List<NodeHandler<?>> handlerBeans;
    private Map<Class<? extends Node>, NodeHandler<? extends Node>> handlers = new HashMap<>();

    @Autowired
    private CampaignRepository campaignRepository;


    @PostConstruct
    public void initHandlers() {
        for (NodeHandler<?> handler : handlerBeans) {
            handlers.put(handler.getHandledType(), handler);
            System.out.println("Registered handler: " + handler.getClass().getSimpleName()
                    + " for node: " + handler.getHandledType().getSimpleName());
        }
    }

    public void handleEvent(EventDTO event) {
        System.out.println("Handling event: " + event.getEventType());



        // Guard: if the event has no leadId we can't route workflows; skip gracefully.
        if (event.getLeadId() == null) {
            System.out.println("Event has no leadId - skipping workflow processing for event: " + event.getEventType());
            return;
        }

        // Guard: if the event has no leadId we can't route workflows; skip gracefully.
        if (event.getLeadId() == null) {
            System.out.println("Event has no leadId - skipping workflow processing for event: " + event.getEventType());
            return;
        }

        Lead eventLead = leadRepository.findById(event.getLeadId()).orElse(null);
        if (eventLead == null) {
            System.out.println("Lead not found for ID: " + event.getLeadId());
            return;
        }

        List<Long> lead_segments = eventLead.getSegmentIds() != null ? eventLead.getSegmentIds() : new ArrayList<>();
        List<Campaign> campaigns_with_lead_segments = campaignRepository.findAll().stream()
                .filter(c -> c.getSegment() != null && !c.getSegment().isEmpty())
                .filter(c -> {
                    for (Segments seg : c.getSegment()) {
                        if (lead_segments.contains(seg.getId())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

        List<Node> target_triggers = new ArrayList<>();
        for (Campaign camp : campaigns_with_lead_segments) {
            // Only process ACTIVE campaigns
            if (camp.getStatus() != CampaignStatus.ACTIVE) {
                System.out.println("Skipping campaign ID: " + camp.getId() + " due to inactive status.");
                continue;
            }
            
            Workflow wf = camp.getWorkflow();
            if (wf == null) continue;
            
            // Only process workflows that are ready to use
            if (!Boolean.TRUE.equals(wf.getIsReadyToUse())) {
                System.out.println("Skipping workflow ID: " + wf.getId() + " as it is not ready to use.");
                continue;
            }
            List<Node> wf_triggers = nodeRepository.findByWorkflowAndEventType(wf, event.getEventType());
            target_triggers.addAll(wf_triggers);
        }
       
        
        for (Node trigger : target_triggers) {
            System.out.println("Trigger event type: " + trigger.getEventType());
            System.out.println(event.getEventType().equals(trigger.getEventType()) ? "Event types match." : "Event types do not match.");
            if (!trigger.matchesEvent(event)) {
                System.out.println("Trigger did not match event criteria.");
                continue;
            }

            System.out.println("Trigger matched ID: " + trigger.getId());

            WorkflowInstance instance = workflowInstanceRepository
                    .findByWorkflowAndLead(trigger.getWorkflow(), eventLead);

            if (instance == null) {
                instance = new WorkflowInstance();
                instance.setLead(eventLead);
                instance.setWorkflow(trigger.getWorkflow());
                instance.getActiveNodes().add(trigger);
                workflowInstanceRepository.save(instance);
                System.out.println("Created new WorkflowInstance for lead ID: " + eventLead.getId());
            } else {
                // Optionally: mark trigger completed and add to active nodes
                if (!instance.getCompletedNodeIds().contains(trigger.getId())) {
                    instance.getActiveNodes().add(trigger);
                }
            }

            runWorkflow(instance);
        }

        System.out.println("Event handling completed for event: " + event.getEventType());
    }

    @SuppressWarnings("unchecked")
    public void runWorkflow(WorkflowInstance instance) {
        System.out.println("Running workflow instance ID: " + instance.getId());
        System.out.println("Active nodes count: " + instance.getActiveNodes().size());
        while (!instance.getActiveNodes().isEmpty()) {
            Node currentNode = instance.getActiveNodes().iterator().next();
            instance.getActiveNodes().remove(currentNode);

            

            if (instance.getCompletedNodeIds().contains(currentNode.getId())) {
                System.out.println("Node ID: " + currentNode.getId() + " already completed; skipping.");
                continue;
            }

            NodeHandler<Node> handler = (NodeHandler<Node>) handlers.get(currentNode.getClass());

            if (handler == null) {
                // fallback for proxies / inheritance
                handler = handlers.values().stream()
                        .filter(h -> h.getHandledType().isInstance(currentNode))
                        .map(h -> (NodeHandler<Node>) h)
                        .findFirst()
                        .orElse(null);
            }

            if (handler != null) {
                System.out.println("Executing node ID: " + currentNode.getEventType() + " with handler: " + handler.getClass().getSimpleName());
                handler.execute(currentNode, instance);
                System.out.println("Executed node ID: " + currentNode.getEventType());
            } else {
                System.out.println("No handler found for node ID: " + currentNode.getId());
            }

            workflowInstanceRepository.save(instance);
        }

        // Mark instance finished when all non-start nodes are completed
        Workflow wf = instance.getWorkflow();
        if (wf != null && wf.getNodes() != null) {
            java.util.Set<Long> nonStartNodeIds = wf.getNodes().stream()
                    .filter(n -> n.getId() != null && n.getNodeType() != NodeType.START)
                    .map(Node::getId)
                    .collect(Collectors.toSet());

            java.util.Set<Long> completedIds = instance.getCompletedNodeIds() != null
                    ? instance.getCompletedNodeIds()
                    : Collections.emptySet();

            // If all non-start nodes are completed, mark finished
            if (completedIds.containsAll(nonStartNodeIds) && !instance.isFinished()) {
                instance.setFinished(true);
                workflowInstanceRepository.save(instance);
                System.out.println("WorkflowInstance ID: " + instance.getId() + " marked finished=true (all non-start nodes completed)");
            }
        }
    }
    
    /**
     * Continue workflow execution from a specific node's outgoing nodes
     */
    public void continueWorkflowFromNode(WorkflowInstance instance, Node fromNode) {
        // Add the outgoing nodes of the specified node to active nodes
        System.out.println("Continuing workflow from node ID: " + fromNode.getId());
        instance.getActiveNodes().addAll(fromNode.getOutgoingNodes());
        runWorkflow(instance);
    }
}
