package com.operimus.Marketing.services;

import com.operimus.Marketing.dto.WorkflowDTO;
import com.operimus.Marketing.entities.*;
import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.NodeRepository;
import com.operimus.Marketing.repositories.WorkflowRepository;
import com.operimus.Marketing.component.NodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private NodeFactory nodeFactory;

    @InjectMocks
    private WorkflowService workflowService;

    private Workflow workflow1;
    private Workflow workflow2;
    private Campaign campaign1;
    private Node startNode;
    private Node actionNode;
    private Node endNode;

    @BeforeEach
    void setUp() {
        campaign1 = new Campaign();
        campaign1.setId(1L);
        campaign1.setName("Test Campaign");

        workflow1 = new Workflow();
        workflow1.setId(10L);
        workflow1.setName("Workflow 1");
        workflow1.setDescription("First workflow");
        workflow1.setCampaign(campaign1);
        campaign1.setWorkflow(workflow1);

        workflow2 = new Workflow();
        workflow2.setId(20L);
        workflow2.setName("Workflow 2");
        workflow2.setDescription("Second workflow");

        startNode = new Node();
        startNode.setId(100L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        startNode.setPositionX(50);
        startNode.setPositionY(50);

        actionNode = new Node();
        actionNode.setId(200L);
        actionNode.setNodeType(NodeType.EMAIL);
        actionNode.setPositionX(200);
        actionNode.setPositionY(50);

        endNode = new Node();
        endNode.setId(300L);
        endNode.setNodeType(NodeType.END);
        endNode.setIsEndNode(true);
        endNode.setPositionX(350);
        endNode.setPositionY(50);

        workflow1.getNodes().addAll(List.of(startNode, actionNode, endNode));
        startNode.setWorkflow(workflow1);
        actionNode.setWorkflow(workflow1);
        endNode.setWorkflow(workflow1);
        startNode.getOutgoingNodes().add(actionNode);
        actionNode.getOutgoingNodes().add(endNode);

        when(workflowRepository.findAll()).thenReturn(List.of(workflow1, workflow2));
        when(workflowRepository.findById(10L)).thenReturn(Optional.of(workflow1));
        when(workflowRepository.findById(99L)).thenReturn(Optional.empty());
        when(workflowRepository.findByIdWithNodes(10L)).thenReturn(Optional.of(workflow1));
        when(workflowRepository.findByIdWithNodes(99L)).thenReturn(Optional.empty());
        when(workflowRepository.existsById(10L)).thenReturn(true);
        when(workflowRepository.existsById(99L)).thenReturn(false);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign1));
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

        when(nodeRepository.findById(100L)).thenReturn(Optional.of(startNode));
        when(nodeRepository.findById(200L)).thenReturn(Optional.of(actionNode));
        when(nodeRepository.findById(300L)).thenReturn(Optional.of(endNode));
        when(nodeRepository.findById(999L)).thenReturn(Optional.empty());

        when(workflowRepository.save(any(Workflow.class))).thenAnswer(i -> {
            Workflow w = i.getArgument(0);
            if (w.getId() == null) w.setId(999L);
            return w;
        });

        when(nodeRepository.save(any(Node.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void whenCreateWorkflow_thenReturnSavedWorkflow() {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setName("New Workflow");
        dto.setDescription("Created via DTO");

        Workflow saved = workflowService.createWorkflow(dto);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Workflow");
        assertThat(saved.getDescription()).isEqualTo("Created via DTO");
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(workflowRepository, times(1)).save(saved);
    }

    @Test
    void whenCreateWorkflow_withValidCampaignId_thenLinkToCampaign() {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setName("Linked Workflow");
        dto.setCampaignId(1L);

        Workflow saved = workflowService.createWorkflow(dto);

        assertThat(saved.getCampaign()).isEqualTo(campaign1);
        assertThat(campaign1.getWorkflow()).isEqualTo(saved);
        verify(campaignRepository, times(1)).findById(1L);
        verify(workflowRepository, times(1)).save(saved);
    }

    @Test
    void whenCreateWorkflow_withInvalidCampaignId_thenThrowException() {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setName("Invalid Campaign Link");
        dto.setCampaignId(99L);

        assertThatThrownBy(() -> workflowService.createWorkflow(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");

        verify(campaignRepository, times(1)).findById(99L);
        verify(workflowRepository, never()).save(any());
    }

    @Test
    void whenGetAllWorkflows_thenReturnList() {
        List<Workflow> result = workflowService.getAllWorkflows();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Workflow::getName)
                .containsExactlyInAnyOrder("Workflow 1", "Workflow 2");
        verify(workflowRepository, times(1)).findAll();
    }

    @Test
    void whenGetWorkflowById_withValidId_thenReturnWorkflow() {
        Workflow found = workflowService.getWorkflowById(10L);

        assertThat(found.getName()).isEqualTo("Workflow 1");
        verify(workflowRepository, times(1)).findById(10L);
    }

    @Test
    void whenGetWorkflowById_withInvalidId_thenThrowException() {
        assertThatThrownBy(() -> workflowService.getWorkflowById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Workflow not found");

        verify(workflowRepository, times(1)).findById(99L);
    }

    @Test
    void whenUpdateWorkflow_withValidId_thenUpdateAndPreserveConnections() {
        // Clear existing connections to avoid duplicates
        startNode.getOutgoingNodes().clear();
        actionNode.getOutgoingNodes().clear();
        
        // Mock nodeFactory.updateNode to return the existing node
        when(nodeFactory.updateNode(any(Node.class), any(Node.class))).thenAnswer(invocation -> {
            Node existing = invocation.getArgument(0);
            Node incoming = invocation.getArgument(1);
            existing.setNodeType(incoming.getNodeType());
            existing.setIsStartNode(incoming.getIsStartNode());
            existing.setIsEndNode(incoming.getIsEndNode());
            existing.setPositionX(incoming.getPositionX());
            existing.setPositionY(incoming.getPositionY());
            return existing;
        });
        
        Workflow updateData = new Workflow();
        updateData.setName("Updated Name");
        updateData.setDescription("Updated Desc");

        Node updatedStart = new Node();
        updatedStart.setId(100L);
        updatedStart.setNodeType(NodeType.START);
        updatedStart.setIsStartNode(true);

        Node updatedAction = new Node();
        updatedAction.setId(200L);
        updatedAction.setNodeType(NodeType.EMAIL);

        // Add connections in the update data
        updatedStart.getOutgoingNodes().add(updatedAction);
        updateData.getNodes().addAll(List.of(updatedStart, updatedAction));

        Workflow updated = workflowService.updateWorkflow(10L, updateData);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getNodes()).hasSize(2);
        verify(workflowRepository, times(1)).findByIdWithNodes(10L);
        verify(workflowRepository, times(1)).save(any(Workflow.class));
    }

    @Test
    void whenUpdateWorkflow_withInvalidId_thenThrowEntityNotFound() {
        Workflow updateData = new Workflow();
        updateData.setName("Should Fail");

        assertThatThrownBy(() -> workflowService.updateWorkflow(99L, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Workflow not found");

        verify(workflowRepository, times(1)).findByIdWithNodes(99L);
    }

    @Test
    void whenDeleteWorkflow_withValidId_thenDelete() {
        workflowService.deleteWorkflow(10L);

        verify(workflowRepository, times(1)).existsById(10L);
        verify(workflowRepository, times(1)).deleteById(10L);
    }

    @Test
    void whenDeleteWorkflow_withInvalidId_thenThrowException() {
        assertThatThrownBy(() -> workflowService.deleteWorkflow(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Workflow not found");

        verify(workflowRepository, times(1)).existsById(99L);
        verify(workflowRepository, never()).deleteById(any());
    }

    @Test
    void whenGetNodes_thenReturnNodeList() {
        List<Node> nodes = workflowService.getNodes(10L);

        assertThat(nodes).hasSize(3);
        assertThat(nodes).extracting(Node::getNodeType)
                .containsExactlyInAnyOrder(NodeType.START, NodeType.EMAIL, NodeType.END);
        verify(workflowRepository, times(1)).findById(10L);
    }

    @Test
    void whenAddNode_thenNodeIsAddedAndSaved() {
        Node newNode = new Node();
        newNode.setNodeType(NodeType.EMAIL);
        newNode.setPositionX(400);
        newNode.setPositionY(400);

        // Mock nodeFactory to return the node
        when(nodeFactory.createNode(any(Node.class))).thenAnswer(invocation -> {
            Node node = invocation.getArgument(0);
            return node;
        });
        
        // Mock saveAndFlush to properly set workflow on nodes
        when(workflowRepository.saveAndFlush(any(Workflow.class))).thenAnswer(invocation -> {
            Workflow wf = invocation.getArgument(0);
            // Set workflow on all nodes in the workflow
            for (Node node : wf.getNodes()) {
                node.setWorkflow(wf);
            }
            return wf;
        });

        Node added = workflowService.addNode(10L, newNode);

        assertThat(added).isNotNull();
        assertThat(added.getWorkflow()).isEqualTo(workflow1);
        assertThat(added.getNodeType()).isEqualTo(NodeType.EMAIL);
        verify(nodeFactory, times(1)).createNode(newNode);
        verify(workflowRepository, times(1)).saveAndFlush(workflow1);
    }

    @Test
    void whenGetNode_withValidIds_thenReturnNode() {
        Node found = workflowService.getNode(10L, 100L);

        assertThat(found.getId()).isEqualTo(100L);
        assertThat(found.getIsStartNode()).isTrue();
        verify(workflowRepository, times(1)).findById(10L);
    }

    @Test
    void whenGetNode_withInvalidNodeId_thenThrowException() {
        assertThatThrownBy(() -> workflowService.getNode(10L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Node not found with id 999");

        verify(workflowRepository, times(1)).findById(10L);
    }

    @Test
    void whenRemoveNode_thenNodeIsRemoved() {
        workflowService.removeNode(10L, 200L);

        assertThat(workflow1.getNodes()).doesNotContain(actionNode);
        verify(workflowRepository, times(1)).save(workflow1);
    }

    @Test
    void whenAddConnection_thenConnectionIsCreated() {
        workflowService.addConnection(10L, 100L, 300L);

        assertThat(startNode.getOutgoingNodes()).contains(endNode);
        verify(workflowRepository, times(1)).save(workflow1);
    }

    @Test
    void whenAddConnection_withCycle_thenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> workflowService.addConnection(10L, 300L, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Adding this edge would create a cycle");

        verify(workflowRepository, never()).save(any());
    }

    @Test
    void whenGetConnection_thenReturnOutgoingNodes() {
        Set<Node> outgoing = workflowService.getConnection(10L, 100L);

        assertThat(outgoing).hasSize(1);
        assertThat(outgoing.iterator().next().getId()).isEqualTo(200L);
    }

    @Test
    void whenRemoveConnection_thenConnectionIsRemoved() {
        workflowService.removeConnection(10L, 100L, 200L);

        assertThat(startNode.getOutgoingNodes()).doesNotContain(actionNode);
        verify(workflowRepository, times(1)).save(workflow1);
    }

    @Test
    void whenClearNodes_thenAllNodesRemoved() {
        workflowService.clearNodes(10L);

        assertThat(workflow1.getNodes()).isEmpty();
        verify(workflowRepository, times(1)).save(workflow1);
    }

    @Test
    void whenGetWorkflowWithNodes_thenReturnWithFetchedGraph() {
        Workflow fetched = workflowService.getWorkflowWithNodes(10L);

        assertThat(fetched.getNodes()).hasSize(3);
        assertThat(fetched.getNodes().get(0).getOutgoingNodes()).isNotEmpty();
        verify(workflowRepository, times(1)).findByIdWithNodes(10L);
    }

    @Test
    void whenGetWorkflowWithNodes_withInvalidId_thenThrowEntityNotFound() {
        assertThatThrownBy(() -> workflowService.getWorkflowWithNodes(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Workflow not found with id 99");

        verify(workflowRepository, times(1)).findByIdWithNodes(99L);
    }

    // ===== Workflow Validation Warning Tests =====

    @Test
    void updateStatus_setsReadyToFalse_whenNoStartNode() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(30L);
        workflow.setName("Test Workflow");
        
        Node node1 = new Node();
        node1.setId(300L);
        node1.setNodeType(NodeType.EMAIL);
        node1.setIsStartNode(false);
        
        workflow.addNode(node1);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenNodesEmpty() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(31L);
        workflow.setName("Empty Workflow");
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenDelayNodeMissingDuration() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(32L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(400L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        DelayAction delayNode = new DelayAction();
        delayNode.setId(401L);
        delayNode.setNodeType(NodeType.DELAY);
        delayNode.setDelayUnit("HOURS");
        // Missing delayDuration
        
        startNode.addOutgoingNode(delayNode);
        workflow.addNode(startNode);
        workflow.addNode(delayNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenDelayNodeMissingUnit() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(33L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(500L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        DelayAction delayNode = new DelayAction();
        delayNode.setId(501L);
        delayNode.setNodeType(NodeType.DELAY);
        delayNode.setDelayDuration(30);
        // Missing delayUnit
        
        startNode.addOutgoingNode(delayNode);
        workflow.addNode(startNode);
        workflow.addNode(delayNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToTrue_whenDelayNodeComplete() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(34L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(600L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        DelayAction delayNode = new DelayAction();
        delayNode.setId(601L);
        delayNode.setNodeType(NodeType.DELAY);
        delayNode.setDelayDuration(30);
        delayNode.setDelayUnit("MINUTES");
        
        Node endNode = new Node();
        endNode.setId(602L);
        endNode.setNodeType(NodeType.END);
        endNode.setIsEndNode(true);
        
        startNode.addOutgoingNode(delayNode);
        delayNode.addOutgoingNode(endNode);
        
        workflow.addNode(startNode);
        workflow.addNode(delayNode);
        workflow.addNode(endNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isTrue();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenEmailNodeMissingTemplateId() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(35L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(700L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        SendEmailAction emailNode = new SendEmailAction();
        emailNode.setId(701L);
        emailNode.setNodeType(NodeType.EMAIL);
        emailNode.setSendFrom("test@example.com");
        // Missing emailTemplateId
        
        startNode.addOutgoingNode(emailNode);
        workflow.addNode(startNode);
        workflow.addNode(emailNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenEmailNodeMissingSendFrom() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(36L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(800L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        SendEmailAction emailNode = new SendEmailAction();
        emailNode.setId(801L);
        emailNode.setNodeType(NodeType.EMAIL);
        emailNode.setEmailTemplateId(1L);
        // Missing sendFrom
        
        startNode.addOutgoingNode(emailNode);
        workflow.addNode(startNode);
        workflow.addNode(emailNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenNodesDisconnected() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(37L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(900L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        Node connectedNode = new Node();
        connectedNode.setId(901L);
        connectedNode.setNodeType(NodeType.END);
        connectedNode.setIsEndNode(true);
        
        Node disconnectedNode = new Node();
        disconnectedNode.setId(902L);
        disconnectedNode.setNodeType(NodeType.EMAIL);
        
        startNode.addOutgoingNode(connectedNode);
        // disconnectedNode is not connected
        
        workflow.addNode(startNode);
        workflow.addNode(connectedNode);
        workflow.addNode(disconnectedNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenFormTriggerMissingFormId() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(38L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(1000L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        OnFormSubmittedTrigger formNode = new OnFormSubmittedTrigger();
        formNode.setId(1001L);
        formNode.setNodeType(NodeType.FORM);
        // Missing formId
        
        startNode.addOutgoingNode(formNode);
        workflow.addNode(startNode);
        workflow.addNode(formNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToFalse_whenSegmentNodeMissingSegmentId() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(39L);
        workflow.setName("Test Workflow");
        
        Node startNode = new Node();
        startNode.setId(1100L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        AddToSegmentAction segmentNode = new AddToSegmentAction();
        segmentNode.setId(1101L);
        segmentNode.setNodeType(NodeType.ADD_TO_SEGMENT);
        // Missing segmentId
        
        startNode.addOutgoingNode(segmentNode);
        workflow.addNode(startNode);
        workflow.addNode(segmentNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isFalse();
    }

    @Test
    void updateStatus_setsReadyToTrue_whenAllNodesCompleteAndConnected() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(40L);
        workflow.setName("Complete Workflow");
        
        Node startNode = new Node();
        startNode.setId(1200L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        DelayAction delayNode = new DelayAction();
        delayNode.setId(1201L);
        delayNode.setNodeType(NodeType.DELAY);
        delayNode.setDelayDuration(1);
        delayNode.setDelayUnit("HOURS");
        
        SendEmailAction emailNode = new SendEmailAction();
        emailNode.setId(1202L);
        emailNode.setNodeType(NodeType.EMAIL);
        emailNode.setEmailTemplateId(1L);
        emailNode.setSendFrom("test@example.com");
        
        Node endNode = new Node();
        endNode.setId(1203L);
        endNode.setNodeType(NodeType.END);
        endNode.setIsEndNode(true);
        
        startNode.addOutgoingNode(delayNode);
        delayNode.addOutgoingNode(emailNode);
        emailNode.addOutgoingNode(endNode);
        
        workflow.addNode(startNode);
        workflow.addNode(delayNode);
        workflow.addNode(emailNode);
        workflow.addNode(endNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isTrue();
    }

    @Test
    void updateStatus_setsReadyToTrue_whenStartAndEndNodesOnly() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setId(41L);
        workflow.setName("Simple Workflow");
        
        Node startNode = new Node();
        startNode.setId(1300L);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);
        
        Node endNode = new Node();
        endNode.setId(1301L);
        endNode.setNodeType(NodeType.END);
        endNode.setIsEndNode(true);
        
        startNode.addOutgoingNode(endNode);
        
        workflow.addNode(startNode);
        workflow.addNode(endNode);
        
        // Act
        workflow.updateStatus();
        
        // Assert
        assertThat(workflow.getIsReadyToUse()).isTrue();
    }
}