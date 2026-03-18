package com.operimus.Marketing.services;

import com.operimus.Marketing.dto.EventDTO;
import com.operimus.Marketing.entities.*;
import com.operimus.Marketing.repositories.LeadRepository;
import com.operimus.Marketing.repositories.NodeRepository;
import com.operimus.Marketing.repositories.WorkflowInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.ArrayList;
import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.repositories.CampaignRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class WorkflowEngineTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private NodeHandler<Node> nodeHandler;

    @InjectMocks
    private WorkflowEngine workflowEngine;

    private Lead lead;
    private Workflow workflow;
    private Node node;
    private Node node2;
    private WorkflowInstance instance;

    @BeforeEach
    void setUp() {
        lead = new Lead();
        lead.setId(1L);
        lead.setSegmentIds(List.of(1L));

        workflow = new Workflow();
        workflow.setId(10L);

        node = spy(new Node());
        node.setId(100L);
        node.setWorkflow(workflow);
        node.setEventType(EventType.FORM_SUBMITTED);

        node2 = spy(new Node());
        node2.setId(101L);
        node2.setWorkflow(workflow);
        node2.setEventType(EventType.SEND_EMAIL);

        node.addOutgoingNode(node2);


        instance = new WorkflowInstance();
        instance.setId(200L);
        instance.setLead(lead);
        instance.setWorkflow(workflow);
        instance.setActiveNodes(new HashSet<>());
        instance.setCompletedNodeIds(new HashSet<>());

        // Init handler beans manually for the test
        workflowEngine.handlerBeans = List.of(nodeHandler);
        doReturn(Node.class).when(nodeHandler).getHandledType();
        workflowEngine.initHandlers();
    }

    @Test
    void handleEvent_createsNewWorkflowInstance_ifNoneExists() {
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(lead.getId());

        // Return a campaign that references our workflow and has a matching segment
        Campaign camp = new Campaign();
        camp.setStatus(CampaignStatus.ACTIVE);
        workflow.setIsReadyToUse(true);
        camp.setWorkflow(workflow);
        Segments seg = new Segments();
        seg.setId(1L);
        camp.setSegment(Set.of(seg));
        when(campaignRepository.findAll()).thenReturn(List.of(camp));

        when(nodeRepository.findByWorkflowAndEventType(workflow, EventType.FORM_SUBMITTED)).thenReturn(List.of(node));
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(workflowInstanceRepository.findByWorkflowAndLead(workflow, lead)).thenReturn(null);
        when(workflowInstanceRepository.save(any(WorkflowInstance.class))).thenAnswer(i -> i.getArgument(0));
        
        // Mock node.matchesEvent to return true
        doReturn(true).when(node).matchesEvent(event);

        workflowEngine.handleEvent(event);

        // Should be called twice: once when creating the instance, once after executing the node in runWorkflow
        verify(workflowInstanceRepository, atLeast(1)).save(any(WorkflowInstance.class));
        verify(nodeHandler, atLeastOnce()).execute(any(Node.class), any(WorkflowInstance.class));
    }

    @Test
    void handleEvent_usesExistingWorkflowInstance_ifPresent() {
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(lead.getId());

        instance.getActiveNodes().add(node);

        // Return a campaign that references our workflow and has a matching segment
        com.operimus.Marketing.entities.Campaign camp = new com.operimus.Marketing.entities.Campaign();
        camp.setStatus(CampaignStatus.ACTIVE);
        workflow.setIsReadyToUse(true);
        camp.setWorkflow(workflow);
        com.operimus.Marketing.entities.Segments seg = new com.operimus.Marketing.entities.Segments();
        seg.setId(1L);
        camp.setSegment(Set.of(seg));
        when(campaignRepository.findAll()).thenReturn(List.of(camp));

        when(nodeRepository.findByWorkflowAndEventType(workflow, EventType.FORM_SUBMITTED)).thenReturn(List.of(node));
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(workflowInstanceRepository.findByWorkflowAndLead(workflow, lead)).thenReturn(instance);
        when(workflowInstanceRepository.save(any(WorkflowInstance.class))).thenAnswer(i -> i.getArgument(0));
        
        // Mock node.matchesEvent to return true
        doReturn(true).when(node).matchesEvent(event);

        workflowEngine.handleEvent(event);

        // Save is called in runWorkflow after executing each node
        verify(workflowInstanceRepository, atLeastOnce()).save(any(WorkflowInstance.class));
        verify(nodeHandler, atLeastOnce()).execute(any(Node.class), any(WorkflowInstance.class));
    }

    @Test
    void handleEvent_doesNothing_ifLeadNotFound() {
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(999L);

        when(leadRepository.findById(999L)).thenReturn(Optional.empty());

        workflowEngine.handleEvent(event);

        verify(workflowInstanceRepository, never()).save(any());
        verify(nodeHandler, never()).execute(any(), any());
    }

    @Test
    void runWorkflow_executesNodes_andMarksCompleted() {
        instance.getActiveNodes().add(node);
        instance.getCompletedNodeIds().clear();

        workflowEngine.runWorkflow(instance);

        verify(nodeHandler, atLeastOnce()).execute(eq(node), eq(instance));
        assertThat(instance.getActiveNodes()).isEmpty();
    }

    // ===== Delay Action Tests =====

    @Test
    void handleEvent_withDelayNode_schedulesDelayAndDoesNotContinueImmediately() {
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(lead.getId());

        // Create workflow with delay node
        DelayAction delayNode = spy(new DelayAction());
        delayNode.setId(150L);
        delayNode.setWorkflow(workflow);
        delayNode.setEventType(EventType.FORM_SUBMITTED);
        delayNode.setDelayDuration(1);
        delayNode.setDelayUnit("HOURS");

        Node nextNode = spy(new Node());
        nextNode.setId(151L);
        nextNode.setWorkflow(workflow);
        nextNode.setEventType(EventType.SEND_EMAIL);
        
        delayNode.addOutgoingNode(nextNode);

        Campaign camp = new Campaign();
        camp.setStatus(CampaignStatus.ACTIVE);
        workflow.setIsReadyToUse(true);
        camp.setWorkflow(workflow);
        Segments seg = new Segments();
        seg.setId(1L);
        camp.setSegment(Set.of(seg));
        when(campaignRepository.findAll()).thenReturn(List.of(camp));

        when(nodeRepository.findByWorkflowAndEventType(workflow, EventType.FORM_SUBMITTED)).thenReturn(List.of(delayNode));
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(workflowInstanceRepository.findByWorkflowAndLead(workflow, lead)).thenReturn(null);
        when(workflowInstanceRepository.save(any(WorkflowInstance.class))).thenAnswer(i -> i.getArgument(0));
        
        doReturn(true).when(delayNode).matchesEvent(event);

        workflowEngine.handleEvent(event);

        // Verify delay node was executed
        verify(nodeHandler, atLeastOnce()).execute(any(DelayAction.class), any(WorkflowInstance.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void runWorkflow_withDelayNode_marksDelayAsCompleted() {
        DelayAction delayNode = spy(new DelayAction());
        delayNode.setId(160L);
        delayNode.setWorkflow(workflow);
        delayNode.setNodeType(NodeType.DELAY);
        delayNode.setDelayDuration(30);
        delayNode.setDelayUnit("MINUTES");

        instance.getActiveNodes().add(delayNode);
        instance.getCompletedNodeIds().clear();

        // Configure handler to handle DelayAction
        NodeHandler<DelayAction> delayHandler = mock(NodeHandler.class);
        when(delayHandler.getHandledType()).thenReturn(DelayAction.class);
        when(delayHandler.execute(any(DelayAction.class), any(WorkflowInstance.class))).thenAnswer(invocation -> {
            WorkflowInstance inst = invocation.getArgument(1);
            DelayAction node = invocation.getArgument(0);
            inst.getCompletedNodeIds().add(node.getId());
            return inst;
        });
        
        workflowEngine.handlerBeans = List.of(delayHandler);
        workflowEngine.initHandlers();

        workflowEngine.runWorkflow(instance);

        verify(delayHandler, times(1)).execute(eq(delayNode), eq(instance));
        assertThat(instance.getCompletedNodeIds()).contains(160L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void runWorkflow_withDelayNodeInChain_executesInCorrectOrder() {
        // Arrange - Create a workflow chain: START -> ACTION -> DELAY -> END
        Node startNode = spy(new Node());
        startNode.setId(170L);
        startNode.setWorkflow(workflow);
        startNode.setNodeType(NodeType.START);
        startNode.setIsStartNode(true);

        Node actionNode = spy(new Node());
        actionNode.setId(171L);
        actionNode.setWorkflow(workflow);
        actionNode.setNodeType(NodeType.EMAIL);

        DelayAction delayNode = spy(new DelayAction());
        delayNode.setId(172L);
        delayNode.setWorkflow(workflow);
        delayNode.setNodeType(NodeType.DELAY);
        delayNode.setDelayDuration(1);
        delayNode.setDelayUnit("HOURS");

        Node endNode = spy(new Node());
        endNode.setId(173L);
        endNode.setWorkflow(workflow);
        endNode.setNodeType(NodeType.END);
        endNode.setIsEndNode(true);

        startNode.addOutgoingNode(actionNode);
        actionNode.addOutgoingNode(delayNode);
        delayNode.addOutgoingNode(endNode);

        instance.getActiveNodes().add(startNode);
        instance.getCompletedNodeIds().clear();

        // Configure handlers
        NodeHandler<Node> basicHandler = mock(NodeHandler.class);
        when(basicHandler.getHandledType()).thenReturn(Node.class);
        when(basicHandler.execute(any(Node.class), any(WorkflowInstance.class))).thenAnswer(invocation -> {
            WorkflowInstance inst = invocation.getArgument(1);
            Node n = invocation.getArgument(0);
            return n.executeNodeLogic(inst);
        });

        NodeHandler<DelayAction> delayHandler = mock(NodeHandler.class);
        when(delayHandler.getHandledType()).thenReturn(DelayAction.class);
        when(delayHandler.execute(any(DelayAction.class), any(WorkflowInstance.class))).thenAnswer(invocation -> {
            WorkflowInstance inst = invocation.getArgument(1);
            DelayAction n = invocation.getArgument(0);
            inst.getCompletedNodeIds().add(n.getId());
            // Delay nodes should NOT add outgoing nodes to active nodes immediately
            return inst;
        });

        workflowEngine.handlerBeans = List.of(basicHandler, delayHandler);
        workflowEngine.initHandlers();

        // Act
        workflowEngine.runWorkflow(instance);

        // Assert - delay node should be completed but next node should NOT be in active nodes
        assertThat(instance.getCompletedNodeIds()).contains(172L);
        verify(delayHandler, times(1)).execute(eq(delayNode), eq(instance));
    }

    @Test
    void runWorkflow_doesNotExecuteAlreadyCompletedNodes() {
        // Arrange
        node.setId(180L);
        instance.getActiveNodes().add(node);
        instance.getCompletedNodeIds().add(180L); // Already completed

        // Act
        workflowEngine.runWorkflow(instance);

        // Assert - should skip already completed nodes
        verify(nodeHandler, never()).execute(eq(node), eq(instance));
        assertThat(instance.getActiveNodes()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void runWorkflow_handlesMultipleDelayNodesInParallel() {
        // Arrange - two delay nodes as outgoing nodes from start
        Node startNode = spy(new Node());
        startNode.setId(190L);
        startNode.setWorkflow(workflow);
        startNode.setNodeType(NodeType.START);

        DelayAction delay1 = spy(new DelayAction());
        delay1.setId(191L);
        delay1.setWorkflow(workflow);
        delay1.setNodeType(NodeType.DELAY);
        delay1.setDelayDuration(30);
        delay1.setDelayUnit("MINUTES");

        DelayAction delay2 = spy(new DelayAction());
        delay2.setId(192L);
        delay2.setWorkflow(workflow);
        delay2.setNodeType(NodeType.DELAY);
        delay2.setDelayDuration(1);
        delay2.setDelayUnit("HOURS");

        startNode.addOutgoingNode(delay1);
        startNode.addOutgoingNode(delay2);

        instance.getActiveNodes().add(startNode);
        instance.getCompletedNodeIds().clear();

        NodeHandler<Node> basicHandler = mock(NodeHandler.class);
        when(basicHandler.getHandledType()).thenReturn(Node.class);
        when(basicHandler.execute(any(Node.class), any(WorkflowInstance.class))).thenAnswer(invocation -> {
            WorkflowInstance inst = invocation.getArgument(1);
            Node n = invocation.getArgument(0);
            return n.executeNodeLogic(inst);
        });

        NodeHandler<DelayAction> delayHandler = mock(NodeHandler.class);
        when(delayHandler.getHandledType()).thenReturn(DelayAction.class);
        when(delayHandler.execute(any(DelayAction.class), any(WorkflowInstance.class))).thenAnswer(invocation -> {
            WorkflowInstance inst = invocation.getArgument(1);
            DelayAction n = invocation.getArgument(0);
            inst.getCompletedNodeIds().add(n.getId());
            return inst;
        });

        workflowEngine.handlerBeans = List.of(basicHandler, delayHandler);
        workflowEngine.initHandlers();

        // Act
        workflowEngine.runWorkflow(instance);

        // Assert - both delay nodes should be completed
        assertThat(instance.getCompletedNodeIds()).contains(191L, 192L);
        verify(delayHandler, times(2)).execute(any(DelayAction.class), eq(instance));
    }

}
