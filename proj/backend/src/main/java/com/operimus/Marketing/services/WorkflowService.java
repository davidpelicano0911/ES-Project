package com.operimus.Marketing.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.operimus.Marketing.dto.WorkflowDTO;
import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.entities.NodeType;
import com.operimus.Marketing.entities.OnFormSubmittedTrigger;
import com.operimus.Marketing.entities.SendEmailAction;
import com.operimus.Marketing.entities.AddToSegmentAction;
import com.operimus.Marketing.entities.RemoveFromSegmentAction;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.entities.WorkflowTemplate;
import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.WorkflowRepository;
import com.operimus.Marketing.repositories.WorkflowTemplateRepository;
import com.operimus.Marketing.repositories.NodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.operimus.Marketing.component.NodeFactory;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class WorkflowService {
    
    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private NodeFactory nodeFactory;

    @Autowired
    private WorkflowTemplateRepository workflowTemplateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Workflow createWorkflow(WorkflowDTO request) {
        Workflow workflow = new Workflow();
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setCreatedAt(new Date());
        workflow.updateStatus();

        if (request.getCampaignId() != null) {
            Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
            workflow.setCampaign(campaign);
            campaign.setWorkflow(workflow);
        }

        return workflowRepository.save(workflow);
    }

    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    public Workflow getWorkflowById(Long id) {
        return workflowRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Workflow not found with id " + id));
    }

    @Transactional
    public Workflow updateWorkflow(Long id, Workflow updatedWorkflow) {
        Workflow existingWorkflow = workflowRepository.findByIdWithNodes(id)
            .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        // Update simple workflow fields (name, description, etc.)
        if (updatedWorkflow.getName() != null) {
            existingWorkflow.setName(updatedWorkflow.getName());
        }
        if (updatedWorkflow.getDescription() != null) {
            existingWorkflow.setDescription(updatedWorkflow.getDescription());
        }

        // --- MAP Setup ---
        // 1. Map existing DB nodes by ID for quick lookup
        Map<Long, Node> existingNodesMap = existingWorkflow.getNodes().stream()
            .collect(Collectors.toMap(Node::getId, Function.identity()));

        // 2. Map to hold the FINAL nodes (both updated existing & newly created)
        // Key = ID (or TempID for new nodes), Value = The Managed JPA Entity
        Map<Object, Node> processedNodesMap = new HashMap<>();
        
        List<Node> finalNodeList = new ArrayList<>();

        // --- PASS 1: Create/Update Nodes & Set Properties ---
        for (Node incoming : updatedWorkflow.getNodes()) {
            System.out.println("Instance of incoming node: " + incoming.getClass().getSimpleName());
            System.out.println("Incoming node ID: " + incoming.getId());
            System.out.println("Incoming node type: " + incoming.getNodeType());
            System.out.println("Incoming node eventType: " + incoming.getEventType());
            System.out.println("Incoming node positionX: " + incoming.getPositionX());
            System.out.println("Incoming node positionY: " + incoming.getPositionY());
            System.out.println("Incoming node isStartNode: " + incoming.getIsStartNode());
            System.out.println("Incoming node isEndNode: " + incoming.getIsEndNode());
            System.out.println("Incoming node outgoingNodes count: " + (incoming.getOutgoingNodes() != null ? incoming.getOutgoingNodes().size() : 0));
            System.out.println("Incoming workflow ID: " + (incoming.getWorkflow() != null ? incoming.getWorkflow().getId() : "null"));
            Node nodeToSave;

            if (incoming instanceof SendEmailAction) {
                System.out.println("Incoming node is SendEmailAction with emailTemplateId: " + ((SendEmailAction) incoming).getEmailTemplateId());
            } else if (incoming instanceof OnFormSubmittedTrigger) {
                System.out.println("Incoming node is OnFormSubmittedTrigger with formId: " + ((OnFormSubmittedTrigger) incoming).getFormId());
            } else if (incoming instanceof AddToSegmentAction) {
                System.out.println("Incoming node is AddToSegmentAction with segmentId: " + ((AddToSegmentAction) incoming).getSegmentId());
            } else if (incoming instanceof RemoveFromSegmentAction) {
                System.out.println("Incoming node is RemoveFromSegmentAction with segmentId: " + ((RemoveFromSegmentAction) incoming).getSegmentId());
            }

            // Is this an existing node?
            if (incoming.getId() != null && existingNodesMap.containsKey(incoming.getId())) {
                nodeToSave = existingNodesMap.get(incoming.getId());
                nodeToSave = nodeFactory.updateNode(nodeToSave, incoming);
            } else {
                // It's a new node
                nodeToSave = nodeFactory.createNode(incoming);
                nodeToSave.setWorkflow(existingWorkflow);
            }

            // Add to our maps/lists
            finalNodeList.add(nodeToSave);
            
            // Identify the node in our map using the node ID
            Long nodeId = incoming.getId();
            if (nodeId != null) {
                processedNodesMap.put(nodeId, nodeToSave);
            } else {
                System.out.println("Warning: Node has null ID, cannot add to processed map");
            }
        }

        // Replace the collection
        existingWorkflow.getNodes().clear();
        existingWorkflow.getNodes().addAll(finalNodeList);

        // --- PASS 2: Rebuild Connections ---
        // Clear old connections first (filter out nulls for safety)
        existingWorkflow.getNodes().stream()
            .filter(Objects::nonNull)
            .forEach(n -> n.getOutgoingNodes().clear());

        for (Node incoming : updatedWorkflow.getNodes()) {
            // Find the source node we just updated/created in Pass 1
            Long sourceId = incoming.getId();
            Node sourceNode = processedNodesMap.get(sourceId);

            if (sourceNode != null && incoming.getOutgoingNodes() != null) {
                System.out.println("Processing connections for node " + sourceId + " with " + incoming.getOutgoingNodes().size() + " outgoing connections");
                
                for (Node shallowTarget : incoming.getOutgoingNodes()) {
                    Long targetId = shallowTarget.getId();
                    System.out.println("Looking for target node with ID: " + targetId);
                    
                    if (targetId != null) {
                        Node realTargetNode = processedNodesMap.get(targetId);
                        if (realTargetNode != null) {
                            System.out.println("Connecting node " + sourceId + " to node " + targetId);
                            sourceNode.addOutgoingNode(realTargetNode);
                        } else {
                            System.out.println("Warning: Target node " + targetId + " not found in processed nodes map");
                            System.out.println("Available node IDs: " + processedNodesMap.keySet());
                        }
                    } else {
                        System.out.println("Warning: Shallow target has null ID");
                    }
                }
            } else {
                if (sourceNode == null) {
                    System.out.println("Warning: Source node " + sourceId + " not found in processed nodes map");
                }
            }
        }

        // Update workflow status based on node completeness
        existingWorkflow.updateStatus();

        return workflowRepository.save(existingWorkflow);
    }



    public void deleteWorkflow(Long id) {
        if (!workflowRepository.existsById(id)) {
            throw new IllegalArgumentException("Workflow not found with id " + id);
        }
        workflowRepository.deleteById(id);
    }

    public List<Node> getNodes(Long workflowId) {
        Workflow wf = getWorkflowById(workflowId);
        return wf.getNodes();
    }

    public Node addNode(Long workflowId, Node node) {
        Workflow wf = getWorkflowById(workflowId);
        Node createdNode = nodeFactory.createNode(node);

        wf.addNode(createdNode);
        wf.updateStatus();
        wf = workflowRepository.saveAndFlush(wf);

        return wf.getNodes().get(wf.getNodes().size() - 1);
    }

    public Node getNode(Long workflowId, Long nodeId) {
        Workflow wf = getWorkflowById(workflowId);
        return wf.getNodes().stream()
            .filter(n -> n.getId() != null && n.getId().equals(nodeId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Node not found with id " + nodeId));
    }

    public void removeNode(Long workflowId, Long nodeId) {
        Workflow wf = getWorkflowById(workflowId);
        Node node = getNode(workflowId, nodeId);
        wf.removeNode(node);
        wf.updateStatus();
        workflowRepository.save(wf);
    }


    public void addConnection(Long workflowId, Long fromNodeId, Long toNodeId) {
        Workflow wf = getWorkflowById(workflowId);
        Node fromNode = getNode(workflowId, fromNodeId);
        Node toNode = getNode(workflowId, toNodeId);
        fromNode.addOutgoingNode(toNode);
        wf.updateStatus();
        workflowRepository.save(wf);
    }

    public Set<Node> getConnection(Long workflowId, Long fromNodeId) {
        Node fromNode = getNode(workflowId, fromNodeId);
        return fromNode.getOutgoingNodes();
    }

    public void removeConnection(Long workflowId, Long fromNodeId, Long toNodeId) {
        Workflow wf = getWorkflowById(workflowId);
        Node fromNode = getNode(workflowId, fromNodeId);
        Node toNode = getNode(workflowId, toNodeId);
        fromNode.removeOutgoingNode(toNode);
        wf.updateStatus();
        workflowRepository.save(wf);
    }

    public void clearNodes(Long workflowId) {
        Workflow workflow = getWorkflowById(workflowId);
        workflow.getNodes().clear();
        workflowRepository.save(workflow);
    }

    public Workflow getWorkflowWithNodes(Long id) {
        return workflowRepository.findByIdWithNodes(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id " + id));
    }

    public Workflow createWorkflowFromTemplate(Long templateId, Long campaignId) {
        System.out.println("Creating workflow from template " + templateId + " for campaign " + campaignId);
        WorkflowTemplate template = workflowTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id " + templateId));
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id " + campaignId));

        Workflow workflow = new Workflow();
        workflow.setName(template.getName());
        workflow.setDescription(template.getDescription());
        workflow.setCampaign(campaign);

        System.out.println("Template data: " + template.getTemplateData());
        if (template.getTemplateData() != null) {
            try {
                JsonNode root = objectMapper.readTree(template.getTemplateData());
                List<Node> createdNodes = new ArrayList<>();

                for (JsonNode n : root.get("nodes")) {
                    Node node = new Node();
                    node.setPositionX(n.get("position").get("x").asInt());
                    node.setPositionY(n.get("position").get("y").asInt());
                    node.setIsStartNode(n.path("data").path("isStartNode").asBoolean(false));
                    node.setIsEndNode(n.path("data").path("isEndNode").asBoolean(false));
                    node.setScore(0);
                    String nodeType = n.path("data").path("nodeType").asText("START");
                    System.out.println("Creating node of type: " + nodeType);
                    if (nodeType.contains("Email")) {
                        nodeType = "EMAIL";
                    } else if(nodeType.contains("Form")) {
                        nodeType = "FORM";
                    }
                    node.setNodeType(NodeType.valueOf(nodeType));
                    node.setWorkflow(workflow);
                    createdNodes.add(node);
                }

                Map<String, Node> idMap = new HashMap<>();
                int i = 0;
                for (JsonNode n : root.get("nodes")) {
                    idMap.put(n.get("id").asText(), createdNodes.get(i++));
                }

                if (root.has("edges")) {
                    for (JsonNode e : root.get("edges")) {
                        String sourceId = e.get("source").asText();
                        String targetId = e.get("target").asText();
                        Node source = idMap.get(sourceId);
                        Node target = idMap.get(targetId);
                        if (source != null && target != null) {
                            source.getOutgoingNodes().add(target);
                        }
                    }
                }

                workflow.setNodes(createdNodes);
            } catch (Exception e) {
                System.out.println("Error parsing workflow template: " + e.getMessage());
                throw new RuntimeException("Error parsing workflow template", e);
            }
        }

        System.out.println("Workflow created from template with " + workflow.getNodes().size() + " nodes.");
        System.out.println("Workflow created: " + workflow);
        Workflow savedWorkflow = workflowRepository.save(workflow);
        campaign.setWorkflow(savedWorkflow);
        campaignRepository.save(campaign);
        if (savedWorkflow.getId() == null) {
            throw new RuntimeException("Workflow ID was not generated correctly.");
        }
        System.out.println("Workflow created successfully with ID: " + savedWorkflow.getId());
        System.out.println("Associated campaign ID: " + (savedWorkflow.getCampaign() != null ? savedWorkflow.getCampaign().getId() : "null"));
        return savedWorkflow;
    }
}