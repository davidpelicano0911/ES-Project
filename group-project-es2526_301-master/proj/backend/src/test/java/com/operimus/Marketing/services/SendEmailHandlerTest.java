package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendEmailHandlerTest {

    @Mock
    private MailJetService mailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private EmailLogService emailLogService;

    @Mock
    private LeadService leadService;

    private SendEmailHandler sendEmailHandler;
    private Lead lead;
    private Workflow workflow;
    private WorkflowInstance instance;

    @BeforeEach
    void setUp() {
        sendEmailHandler = new SendEmailHandler();
        
        // Use reflection to inject mocked dependencies
        try {
            java.lang.reflect.Field mailServiceField = SendEmailHandler.class.getDeclaredField("mailService");
            mailServiceField.setAccessible(true);
            mailServiceField.set(sendEmailHandler, mailService);
            
            java.lang.reflect.Field templateServiceField = SendEmailHandler.class.getDeclaredField("emailTemplateService");
            templateServiceField.setAccessible(true);
            templateServiceField.set(sendEmailHandler, emailTemplateService);
            
            java.lang.reflect.Field emailLogServiceField = SendEmailHandler.class.getDeclaredField("emailLogService");
            emailLogServiceField.setAccessible(true);
            emailLogServiceField.set(sendEmailHandler, emailLogService);
            
            java.lang.reflect.Field leadServiceField = SendEmailHandler.class.getDeclaredField("leadService");
            leadServiceField.setAccessible(true);
            leadServiceField.set(sendEmailHandler, leadService);
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
    void execute_sendsEmail_successfully() {
        // Arrange
        SendEmailAction emailNode = new SendEmailAction();
        emailNode.setId(500L);
        emailNode.setEmailTemplateId(1L);
        emailNode.setSendFrom("noreply@example.com");
        emailNode.setWorkflow(workflow);
        
        EmailTemplate template = new EmailTemplate();
        template.setId(1L);
        template.setSubject("Test Subject");
        template.setBody("Test Body");
        
        when(emailTemplateService.getTemplateById(1L)).thenReturn(Optional.of(template));
        // Fix: sendEmail returns Long, not void - use when().thenReturn() instead of doNothing()
        when(mailService.sendEmail(any(Lead.class), anyString(), anyString(), anyString())).thenReturn(123456L);
        
        // Act
        WorkflowInstance result = sendEmailHandler.execute(emailNode, instance);
        
        // Assert
        verify(emailTemplateService, times(1)).getTemplateById(1L);
        verify(mailService, times(1)).sendEmail(eq(lead), eq("noreply@example.com"), eq("Test Subject"), eq("Test Body"));
        // logEmail is called with: campaignId (null), leadId (from instance), email (from lead), messageId, subject
        verify(emailLogService, times(1)).logEmail(null, lead.getId(), lead.getEmail(), 123456L, "Test Subject");
        assertThat(result).isNotNull();
    }
    
    @Test
    void execute_throwsIllegalStateException_whenTemplateNotFound() {
        // Arrange
        SendEmailAction emailNode = new SendEmailAction();
        emailNode.setId(500L);
        emailNode.setEmailTemplateId(999L);
        emailNode.setSendFrom("noreply@example.com");
        
        when(emailTemplateService.getTemplateById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> sendEmailHandler.execute(emailNode, instance))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email template not found with ID: 999");
        
        verify(emailTemplateService, times(1)).getTemplateById(999L);
        verify(mailService, never()).sendEmail(any(), any(), any(), any());
    }
    
    @Test
    void getHandledType_returnsSendEmailActionClass() {
        assertThat(sendEmailHandler.getHandledType()).isEqualTo(SendEmailAction.class);
    }

    @Test
    void execute_marksNodeAsCompleted() {
        // Arrange
        SendEmailAction emailNode = new SendEmailAction();
        emailNode.setId(500L);
        emailNode.setEmailTemplateId(1L);
        emailNode.setSendFrom("noreply@example.com");
        emailNode.setWorkflow(workflow);
        
        instance.getActiveNodes().add(emailNode);
        
        EmailTemplate template = new EmailTemplate();
        template.setId(1L);
        template.setSubject("Test Subject");
        template.setBody("Test Body");
        
        when(emailTemplateService.getTemplateById(1L)).thenReturn(Optional.of(template));
        // Fix: sendEmail returns Long, not void - use when().thenReturn() instead of doNothing()
        when(mailService.sendEmail(any(Lead.class), anyString(), anyString(), anyString())).thenReturn(123456L);
        
        // Act
        WorkflowInstance result = sendEmailHandler.execute(emailNode, instance);
        
        // Assert
        verify(emailLogService, times(1)).logEmail(null, lead.getId(), lead.getEmail(), 123456L, "Test Subject");
        assertThat(result.getCompletedNodeIds()).contains(emailNode.getId());
        assertThat(result.getActiveNodes()).doesNotContain(emailNode);
    }
}
