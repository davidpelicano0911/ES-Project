package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.*;
import com.operimus.Marketing.entities.DelayedExecution.DelayedExecutionStatus;
import com.operimus.Marketing.repository.DelayedExecutionRepository;
import com.operimus.Marketing.repositories.NodeRepository;
import com.operimus.Marketing.repositories.WorkflowInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelaySchedulerServiceTest {

    @Mock
    private DelayedExecutionRepository delayedExecutionRepository;

    @Mock
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private WorkflowEngine workflowEngine;

    private DelaySchedulerService delaySchedulerService;

    @BeforeEach
    void setUp() {
        delaySchedulerService = new DelaySchedulerService();
        
        // Use reflection to inject mocked dependencies
        try {
            java.lang.reflect.Field repoField = DelaySchedulerService.class.getDeclaredField("delayedExecutionRepository");
            repoField.setAccessible(true);
            repoField.set(delaySchedulerService, delayedExecutionRepository);
            
            java.lang.reflect.Field instanceRepoField = DelaySchedulerService.class.getDeclaredField("workflowInstanceRepository");
            instanceRepoField.setAccessible(true);
            instanceRepoField.set(delaySchedulerService, workflowInstanceRepository);
            
            java.lang.reflect.Field nodeRepoField = DelaySchedulerService.class.getDeclaredField("nodeRepository");
            nodeRepoField.setAccessible(true);
            nodeRepoField.set(delaySchedulerService, nodeRepository);
            
            java.lang.reflect.Field contextField = DelaySchedulerService.class.getDeclaredField("applicationContext");
            contextField.setAccessible(true);
            contextField.set(delaySchedulerService, applicationContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void scheduleDelayedExecution_createsAndSavesPendingExecution() {
        // Arrange
        Long workflowInstanceId = 1L;
        Long nodeId = 100L;
        LocalDateTime executionTime = LocalDateTime.now().plusHours(1);
        
        ArgumentCaptor<DelayedExecution> captor = ArgumentCaptor.forClass(DelayedExecution.class);
        
        // Act
        delaySchedulerService.scheduleDelayedExecution(workflowInstanceId, nodeId, executionTime);
        
        // Assert
        verify(delayedExecutionRepository, times(1)).save(captor.capture());
        DelayedExecution saved = captor.getValue();
        
        assertThat(saved.getWorkflowInstanceId()).isEqualTo(workflowInstanceId);
        assertThat(saved.getNodeId()).isEqualTo(nodeId);
        assertThat(saved.getExecutionTime()).isEqualTo(executionTime);
        assertThat(saved.getStatus()).isEqualTo(DelayedExecutionStatus.PENDING);
    }

    @Test
    void processDelayedExecutions_executesReadyExecutions() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        DelayedExecution execution1 = new DelayedExecution();
        execution1.setId(1L);
        execution1.setWorkflowInstanceId(10L);
        execution1.setNodeId(100L);
        execution1.setExecutionTime(now.minusMinutes(5));
        execution1.setStatus(DelayedExecutionStatus.PENDING);
        
        DelayedExecution execution2 = new DelayedExecution();
        execution2.setId(2L);
        execution2.setWorkflowInstanceId(11L);
        execution2.setNodeId(101L);
        execution2.setExecutionTime(now.minusMinutes(1));
        execution2.setStatus(DelayedExecutionStatus.PENDING);
        
        List<DelayedExecution> readyExecutions = Arrays.asList(execution1, execution2);
        
        when(delayedExecutionRepository.findExecutionsReadyToRun(any(LocalDateTime.class), eq(DelayedExecutionStatus.PENDING)))
            .thenReturn(readyExecutions);
        
        // Setup workflow instances and nodes
        WorkflowInstance instance1 = createWorkflowInstance(10L);
        WorkflowInstance instance2 = createWorkflowInstance(11L);
        Node node1 = createNode(100L);
        Node node2 = createNode(101L);
        
        when(workflowInstanceRepository.findById(10L)).thenReturn(Optional.of(instance1));
        when(workflowInstanceRepository.findById(11L)).thenReturn(Optional.of(instance2));
        when(nodeRepository.findById(100L)).thenReturn(Optional.of(node1));
        when(nodeRepository.findById(101L)).thenReturn(Optional.of(node2));
        when(applicationContext.getBean(WorkflowEngine.class)).thenReturn(workflowEngine);
        
        // Act
        delaySchedulerService.processDelayedExecutions();
        
        // Assert
        verify(workflowEngine, times(2)).runWorkflow(any(WorkflowInstance.class));
        verify(delayedExecutionRepository, times(2)).save(any(DelayedExecution.class));
        
        assertThat(execution1.getStatus()).isEqualTo(DelayedExecutionStatus.EXECUTED);
        assertThat(execution1.getExecutedAt()).isNotNull();
        assertThat(execution2.getStatus()).isEqualTo(DelayedExecutionStatus.EXECUTED);
        assertThat(execution2.getExecutedAt()).isNotNull();
    }

    @Test
    void processDelayedExecutions_doesNothingWhenNoReadyExecutions() {
        // Arrange
        when(delayedExecutionRepository.findExecutionsReadyToRun(any(LocalDateTime.class), eq(DelayedExecutionStatus.PENDING)))
            .thenReturn(Collections.emptyList());
        
        // Act
        delaySchedulerService.processDelayedExecutions();
        
        // Assert
        verify(workflowInstanceRepository, never()).findById(anyLong());
        verify(nodeRepository, never()).findById(anyLong());
        verify(delayedExecutionRepository, never()).save(any(DelayedExecution.class));
    }

    @Test
    void processDelayedExecutions_marksAsFailedWhenWorkflowInstanceNotFound() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        DelayedExecution execution = new DelayedExecution();
        execution.setId(1L);
        execution.setWorkflowInstanceId(999L);
        execution.setNodeId(100L);
        execution.setExecutionTime(now.minusMinutes(5));
        execution.setStatus(DelayedExecutionStatus.PENDING);
        
        when(delayedExecutionRepository.findExecutionsReadyToRun(any(LocalDateTime.class), eq(DelayedExecutionStatus.PENDING)))
            .thenReturn(Collections.singletonList(execution));
        
        when(workflowInstanceRepository.findById(999L)).thenReturn(Optional.empty());
        when(nodeRepository.findById(100L)).thenReturn(Optional.of(createNode(100L)));
        
        // Act
        delaySchedulerService.processDelayedExecutions();
        
        // Assert
        verify(delayedExecutionRepository, times(1)).save(execution);
        assertThat(execution.getStatus()).isEqualTo(DelayedExecutionStatus.FAILED);
        verify(workflowEngine, never()).runWorkflow(any(WorkflowInstance.class));
    }

    @Test
    void processDelayedExecutions_marksAsFailedWhenNodeNotFound() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        DelayedExecution execution = new DelayedExecution();
        execution.setId(1L);
        execution.setWorkflowInstanceId(10L);
        execution.setNodeId(999L);
        execution.setExecutionTime(now.minusMinutes(5));
        execution.setStatus(DelayedExecutionStatus.PENDING);
        
        when(delayedExecutionRepository.findExecutionsReadyToRun(any(LocalDateTime.class), eq(DelayedExecutionStatus.PENDING)))
            .thenReturn(Collections.singletonList(execution));
        
        when(workflowInstanceRepository.findById(10L)).thenReturn(Optional.of(createWorkflowInstance(10L)));
        when(nodeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        delaySchedulerService.processDelayedExecutions();
        
        // Assert
        verify(delayedExecutionRepository, times(1)).save(execution);
        assertThat(execution.getStatus()).isEqualTo(DelayedExecutionStatus.FAILED);
        verify(workflowEngine, never()).runWorkflow(any(WorkflowInstance.class));
    }

    @Test
    void processDelayedExecutions_addsOutgoingNodesToActiveNodes() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        DelayedExecution execution = new DelayedExecution();
        execution.setId(1L);
        execution.setWorkflowInstanceId(10L);
        execution.setNodeId(100L);
        execution.setExecutionTime(now.minusMinutes(5));
        execution.setStatus(DelayedExecutionStatus.PENDING);
        
        when(delayedExecutionRepository.findExecutionsReadyToRun(any(LocalDateTime.class), eq(DelayedExecutionStatus.PENDING)))
            .thenReturn(Collections.singletonList(execution));
        
        WorkflowInstance instance = createWorkflowInstance(10L);
        Node delayNode = createNode(100L);
        Node nextNode1 = createNode(101L);
        Node nextNode2 = createNode(102L);
        delayNode.addOutgoingNode(nextNode1);
        delayNode.addOutgoingNode(nextNode2);
        
        when(workflowInstanceRepository.findById(10L)).thenReturn(Optional.of(instance));
        when(nodeRepository.findById(100L)).thenReturn(Optional.of(delayNode));
        when(applicationContext.getBean(WorkflowEngine.class)).thenReturn(workflowEngine);
        
        // Act
        delaySchedulerService.processDelayedExecutions();
        
        // Assert
        assertThat(instance.getActiveNodes()).containsExactlyInAnyOrder(nextNode1, nextNode2);
        verify(workflowEngine, times(1)).runWorkflow(instance);
    }

    @Test
    void cancelDelayedExecutions_cancelsAllPendingExecutions() {
        // Arrange
        Long workflowInstanceId = 10L;
        
        DelayedExecution execution1 = new DelayedExecution();
        execution1.setId(1L);
        execution1.setWorkflowInstanceId(workflowInstanceId);
        execution1.setStatus(DelayedExecutionStatus.PENDING);
        
        DelayedExecution execution2 = new DelayedExecution();
        execution2.setId(2L);
        execution2.setWorkflowInstanceId(workflowInstanceId);
        execution2.setStatus(DelayedExecutionStatus.PENDING);
        
        List<DelayedExecution> pendingExecutions = Arrays.asList(execution1, execution2);
        
        when(delayedExecutionRepository.findByWorkflowInstanceIdAndStatus(workflowInstanceId, DelayedExecutionStatus.PENDING))
            .thenReturn(pendingExecutions);
        
        // Act
        delaySchedulerService.cancelDelayedExecutions(workflowInstanceId);
        
        // Assert
        assertThat(execution1.getStatus()).isEqualTo(DelayedExecutionStatus.CANCELLED);
        assertThat(execution2.getStatus()).isEqualTo(DelayedExecutionStatus.CANCELLED);
        verify(delayedExecutionRepository, times(1)).saveAll(pendingExecutions);
    }

    @Test
    void cancelDelayedExecutions_doesNothingWhenNoPendingExecutions() {
        // Arrange
        Long workflowInstanceId = 10L;
        
        when(delayedExecutionRepository.findByWorkflowInstanceIdAndStatus(workflowInstanceId, DelayedExecutionStatus.PENDING))
            .thenReturn(Collections.emptyList());
        
        // Act
        delaySchedulerService.cancelDelayedExecutions(workflowInstanceId);
        
        // Assert
        verify(delayedExecutionRepository, times(1)).saveAll(Collections.emptyList());
    }

    @Test
    void processDelayedExecutions_handlesMultipleExecutionsForSameInstance() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        DelayedExecution execution1 = new DelayedExecution();
        execution1.setId(1L);
        execution1.setWorkflowInstanceId(10L);
        execution1.setNodeId(100L);
        execution1.setStatus(DelayedExecutionStatus.PENDING);
        
        DelayedExecution execution2 = new DelayedExecution();
        execution2.setId(2L);
        execution2.setWorkflowInstanceId(10L);
        execution2.setNodeId(101L);
        execution2.setStatus(DelayedExecutionStatus.PENDING);
        
        when(delayedExecutionRepository.findExecutionsReadyToRun(any(LocalDateTime.class), eq(DelayedExecutionStatus.PENDING)))
            .thenReturn(Arrays.asList(execution1, execution2));
        
        WorkflowInstance instance = createWorkflowInstance(10L);
        Node node1 = createNode(100L);
        Node node2 = createNode(101L);
        
        when(workflowInstanceRepository.findById(10L)).thenReturn(Optional.of(instance));
        when(nodeRepository.findById(100L)).thenReturn(Optional.of(node1));
        when(nodeRepository.findById(101L)).thenReturn(Optional.of(node2));
        when(applicationContext.getBean(WorkflowEngine.class)).thenReturn(workflowEngine);
        
        // Act
        delaySchedulerService.processDelayedExecutions();
        
        // Assert
        verify(workflowEngine, times(2)).runWorkflow(instance);
        assertThat(execution1.getStatus()).isEqualTo(DelayedExecutionStatus.EXECUTED);
        assertThat(execution2.getStatus()).isEqualTo(DelayedExecutionStatus.EXECUTED);
    }

    @Test
    void processDelayedExecutions_handlesExceptionGracefully() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        DelayedExecution execution = new DelayedExecution();
        execution.setId(1L);
        execution.setWorkflowInstanceId(10L);
        execution.setNodeId(100L);
        execution.setExecutionTime(now.minusMinutes(5));
        execution.setStatus(DelayedExecutionStatus.PENDING);
        
        when(delayedExecutionRepository.findExecutionsReadyToRun(any(LocalDateTime.class), eq(DelayedExecutionStatus.PENDING)))
            .thenReturn(Collections.singletonList(execution));
        
        when(workflowInstanceRepository.findById(10L)).thenReturn(Optional.of(createWorkflowInstance(10L)));
        when(nodeRepository.findById(100L)).thenReturn(Optional.of(createNode(100L)));
        when(applicationContext.getBean(WorkflowEngine.class)).thenReturn(workflowEngine);
        doThrow(new RuntimeException("Workflow execution failed")).when(workflowEngine).runWorkflow(any(WorkflowInstance.class));
        
        // Act
        delaySchedulerService.processDelayedExecutions();
        
        // Assert
        verify(delayedExecutionRepository, times(1)).save(execution);
        assertThat(execution.getStatus()).isEqualTo(DelayedExecutionStatus.FAILED);
    }

    // Helper methods
    private WorkflowInstance createWorkflowInstance(Long id) {
        Lead lead = new Lead();
        lead.setId(1L);
        
        Workflow workflow = new Workflow();
        workflow.setId(1L);
        
        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(id);
        instance.setLead(lead);
        instance.setWorkflow(workflow);
        instance.setActiveNodes(new HashSet<>());
        instance.setCompletedNodeIds(new HashSet<>());
        
        return instance;
    }

    private Node createNode(Long id) {
        Node node = new Node();
        node.setId(id);
        node.setNodeType(NodeType.DELAY);
        return node;
    }
}
