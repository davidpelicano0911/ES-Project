package com.operimus.Marketing.services;

import com.operimus.Marketing.dto.EventDTO;
import com.operimus.Marketing.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OnFormSubmittedTriggerHandlerTest {

    private OnFormSubmittedTriggerHandler handler;
    private Lead lead;
    private Workflow workflow;
    private WorkflowInstance instance;

    @BeforeEach
    void setUp() {
        handler = new OnFormSubmittedTriggerHandler();
        
        lead = new Lead();
        lead.setId(1L);

        workflow = new Workflow();
        workflow.setId(10L);

        instance = new WorkflowInstance();
        instance.setId(300L);
        instance.setLead(lead);
        instance.setWorkflow(workflow);
        instance.setActiveNodes(new HashSet<>());
        instance.setCompletedNodeIds(new HashSet<>());
    }

    @Test
    void execute_completesNodeSuccessfully() {
        // Arrange
        OnFormSubmittedTrigger trigger = new OnFormSubmittedTrigger();
        trigger.setId(600L);
        trigger.setFormId(1L);
        trigger.setWorkflow(workflow);
        
        instance.getActiveNodes().add(trigger);
        
        // Act
        WorkflowInstance result = handler.execute(trigger, instance);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCompletedNodeIds()).contains(trigger.getId());
        assertThat(result.getActiveNodes()).doesNotContain(trigger);
    }
    
    @Test
    void matchesEvent_returnsTrue_withCorrectFormId() {
        // Arrange
        OnFormSubmittedTrigger trigger = new OnFormSubmittedTrigger();
        trigger.setFormId(1L);
        
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(1L);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("formId", 1L);
        event.setMetadata(metadata);
        
        // Act
        boolean matches = trigger.matchesEvent(event);
        
        // Assert
        assertThat(matches).isTrue();
    }
    
    @Test
    void matchesEvent_returnsFalse_withDifferentFormId() {
        // Arrange
        OnFormSubmittedTrigger trigger = new OnFormSubmittedTrigger();
        trigger.setFormId(1L);
        
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(1L);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("formId", 2L);
        event.setMetadata(metadata);
        
        // Act
        boolean matches = trigger.matchesEvent(event);
        
        // Assert
        assertThat(matches).isFalse();
    }
    
    @Test
    void matchesEvent_returnsFalse_withoutFormIdInMetadata() {
        // Arrange
        OnFormSubmittedTrigger trigger = new OnFormSubmittedTrigger();
        trigger.setFormId(1L);
        
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(1L);
        event.setMetadata(new HashMap<>());
        
        // Act
        boolean matches = trigger.matchesEvent(event);
        
        // Assert
        assertThat(matches).isFalse();
    }

    @Test
    void matchesEvent_returnsFalse_withNullFormId() {
        // Arrange
        OnFormSubmittedTrigger trigger = new OnFormSubmittedTrigger();
        trigger.setFormId(null);
        
        EventDTO event = new EventDTO();
        event.setEventType(EventType.FORM_SUBMITTED);
        event.setLeadId(1L);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("formId", 1L);
        event.setMetadata(metadata);
        
        // Act
        boolean matches = trigger.matchesEvent(event);
        
        // Assert
        assertThat(matches).isFalse();
    }
    
    @Test
    void getHandledType_returnsOnFormSubmittedTriggerClass() {
        assertThat(handler.getHandledType()).isEqualTo(OnFormSubmittedTrigger.class);
    }

    @Test
    void execute_addsOutgoingNodesToActiveNodes() {
        // Arrange
        OnFormSubmittedTrigger trigger = new OnFormSubmittedTrigger();
        trigger.setId(600L);
        trigger.setFormId(1L);
        trigger.setWorkflow(workflow);
        
        Node nextNode = new Node();
        nextNode.setId(700L);
        nextNode.setWorkflow(workflow);
        trigger.addOutgoingNode(nextNode);
        
        instance.getActiveNodes().add(trigger);
        
        // Act
        WorkflowInstance result = handler.execute(trigger, instance);
        
        // Assert
        assertThat(result.getActiveNodes()).contains(nextNode);
        assertThat(result.getActiveNodes()).doesNotContain(trigger);
        assertThat(result.getCompletedNodeIds()).contains(trigger.getId());
    }
}
