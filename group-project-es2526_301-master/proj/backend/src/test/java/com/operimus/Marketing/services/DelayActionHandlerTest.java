package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelayActionHandlerTest {

    @Mock
    private DelaySchedulerService delaySchedulerService;

    private DelayActionHandler delayActionHandler;
    private Lead lead;
    private Workflow workflow;
    private WorkflowInstance instance;

    @BeforeEach
    void setUp() {
        delayActionHandler = new DelayActionHandler();
        
        // Use reflection to inject mocked dependencies
        try {
            java.lang.reflect.Field schedulerField = DelayActionHandler.class.getDeclaredField("delaySchedulerService");
            schedulerField.setAccessible(true);
            schedulerField.set(delayActionHandler, delaySchedulerService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        lead = new Lead();
        lead.setId(1L);

        workflow = new Workflow();
        workflow.setId(10L);

        instance = new WorkflowInstance();
        instance.setId(200L);
        instance.setLead(lead);
        instance.setWorkflow(workflow);
        instance.setActiveNodes(new HashSet<>());
        instance.setCompletedNodeIds(new HashSet<>());
    }

    @Test
    void getHandledType_returnsDelayActionClass() {
        assertThat(delayActionHandler.getHandledType()).isEqualTo(DelayAction.class);
    }

    @Test
    void execute_schedulesDelayAndMarksNodeComplete_withMinutes() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(100L);
        delayNode.setDelayDuration(30);
        delayNode.setDelayUnit("MINUTES");
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(100L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(100L);
        assertThat(result).isSameAs(instance);
    }

    @Test
    void execute_schedulesDelayAndMarksNodeComplete_withHours() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(101L);
        delayNode.setDelayDuration(2);
        delayNode.setDelayUnit("HOURS");
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(101L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(101L);
    }

    @Test
    void execute_schedulesDelayAndMarksNodeComplete_withDays() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(102L);
        delayNode.setDelayDuration(1);
        delayNode.setDelayUnit("DAYS");
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(102L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(102L);
    }

    @Test
    void execute_schedulesDelayAndMarksNodeComplete_withSeconds() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(103L);
        delayNode.setDelayDuration(90);
        delayNode.setDelayUnit("SECONDS");
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(103L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(103L);
    }

    @Test
    void execute_doesNotAddOutgoingNodesToActiveNodes() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(104L);
        delayNode.setDelayDuration(1);
        delayNode.setDelayUnit("HOURS");
        delayNode.setWorkflow(workflow);
        
        Node nextNode = new Node();
        nextNode.setId(105L);
        nextNode.setWorkflow(workflow);
        delayNode.addOutgoingNode(nextNode);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert - delay nodes should NOT add outgoing nodes immediately
        assertThat(result.getActiveNodes()).doesNotContain(nextNode);
        assertThat(result.getCompletedNodeIds()).contains(104L);
    }

    @Test
    void execute_handlesNullDelayUnit_defaultsToMinutes() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(106L);
        delayNode.setDelayDuration(15);
        delayNode.setDelayUnit(null);
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(106L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(106L);
    }

    @Test
    void execute_handlesInvalidDelayUnit_defaultsToMinutes() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(107L);
        delayNode.setDelayDuration(5);
        delayNode.setDelayUnit("INVALID_UNIT");
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(107L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(107L);
    }

    @Test
    void execute_handlesZeroDuration() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(108L);
        delayNode.setDelayDuration(0);
        delayNode.setDelayUnit("HOURS");
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert - should still schedule even with 0 duration
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(108L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(108L);
    }

    @Test
    void execute_handlesNullDuration() {
        // Arrange
        DelayAction delayNode = new DelayAction();
        delayNode.setId(109L);
        delayNode.setDelayDuration(null);
        delayNode.setDelayUnit("HOURS");
        delayNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(delayNode);
        
        // Act
        WorkflowInstance result = delayActionHandler.execute(delayNode, instance);
        
        // Assert - should still schedule even with null duration (will be 0)
        verify(delaySchedulerService, times(1)).scheduleDelayedExecution(
            eq(200L), 
            eq(109L), 
            any(LocalDateTime.class)
        );
        assertThat(result.getCompletedNodeIds()).contains(109L);
    }

    @Test
    void execute_multipleDelayNodes_eachScheduledIndependently() {
        // Arrange
        DelayAction delayNode1 = new DelayAction();
        delayNode1.setId(110L);
        delayNode1.setDelayDuration(10);
        delayNode1.setDelayUnit("MINUTES");
        delayNode1.setWorkflow(workflow);
        
        DelayAction delayNode2 = new DelayAction();
        delayNode2.setId(111L);
        delayNode2.setDelayDuration(1);
        delayNode2.setDelayUnit("HOURS");
        delayNode2.setWorkflow(workflow);
        
        // Act
        delayActionHandler.execute(delayNode1, instance);
        delayActionHandler.execute(delayNode2, instance);
        
        // Assert
        verify(delaySchedulerService, times(2)).scheduleDelayedExecution(
            eq(200L), 
            anyLong(), 
            any(LocalDateTime.class)
        );
        assertThat(instance.getCompletedNodeIds()).containsExactlyInAnyOrder(110L, 111L);
    }
}
