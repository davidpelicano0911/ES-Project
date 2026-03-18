package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.EmailLog;
import com.operimus.Marketing.entities.MailjetEventType;
import com.operimus.Marketing.services.EmailLogService;
import com.operimus.Marketing.services.MailJetPollingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for EmailLogController
 * 
 * Tests the REST API endpoints for email log querying including:
 * - Retrieving all email logs
 * - Filtering by campaign
 * - Filtering by lead
 * - Filtering by message ID
 * - Cross-filtering (campaign + lead)
 * - Error handling
 */
@DisplayName("EmailLogController Tests")
@WebMvcTest(EmailLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "api.version=v3",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://example.com/issuer"
})
class EmailLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailLogService emailLogService;

    @MockBean
    private MailJetPollingService mailJetPollingService;

    private EmailLog testEmailLog;

    @BeforeEach
    void setUp() {
        // Create test email log
        testEmailLog = new EmailLog();
        testEmailLog.setId(1L);
        testEmailLog.setMailjetMessageId(12345L);
        testEmailLog.setLeadId(50L);
        testEmailLog.setCampaignId(10L);
        testEmailLog.setEventType(MailjetEventType.SENT);
        testEmailLog.setEmailAddress("test@example.com");
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/all - should return all email logs")
    void testGetAllEmailLogs() throws Exception {
        // Arrange
        List<EmailLog> logs = Arrays.asList(testEmailLog);
        when(emailLogService.getAllEmailLogs()).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].mailjetMessageId", is(12345)))
                .andExpect(jsonPath("$[0].emailAddress", is("test@example.com")));

        verify(emailLogService, times(1)).getAllEmailLogs();
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/all - should return empty list when no logs exist")
    void testGetAllEmailLogsEmpty() throws Exception {
        // Arrange
        when(emailLogService.getAllEmailLogs()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/campaign/{campaignId} - should return logs for campaign")
    void testGetEmailLogsByCampaign() throws Exception {
        // Arrange
        Long campaignId = 10L;
        List<EmailLog> logs = Arrays.asList(testEmailLog);
        when(emailLogService.getEmailLogsByCampaign(campaignId)).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/campaign/{campaignId}", campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].campaignId", is(10)))
                .andExpect(jsonPath("$[0].leadId", is(50)));

        verify(emailLogService, times(1)).getEmailLogsByCampaign(campaignId);
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/campaign/{campaignId} - should return empty list for unknown campaign")
    void testGetEmailLogsByCampaignNotFound() throws Exception {
        // Arrange
        Long campaignId = 999L;
        when(emailLogService.getEmailLogsByCampaign(campaignId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/campaign/{campaignId}", campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/lead/{leadId} - should return logs for lead")
    void testGetEmailLogsByLead() throws Exception {
        // Arrange
        Long leadId = 50L;
        List<EmailLog> logs = Arrays.asList(testEmailLog);
        when(emailLogService.getEmailLogsByLead(leadId)).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/lead/{leadId}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].leadId", is(50)))
                .andExpect(jsonPath("$[0].campaignId", is(10)));

        verify(emailLogService, times(1)).getEmailLogsByLead(leadId);
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/lead/{leadId} - should return empty list for unknown lead")
    void testGetEmailLogsByLeadNotFound() throws Exception {
        // Arrange
        Long leadId = 999L;
        when(emailLogService.getEmailLogsByLead(leadId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/lead/{leadId}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/campaign/{campaignId}/lead/{leadId} - should return filtered logs")
    void testGetEmailLogsByCampaignAndLead() throws Exception {
        // Arrange
        Long campaignId = 10L;
        Long leadId = 50L;
        List<EmailLog> logs = Arrays.asList(testEmailLog);
        when(emailLogService.getEmailLogsByCampaignAndLead(campaignId, leadId)).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/campaign/{campaignId}/lead/{leadId}", campaignId, leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].campaignId", is(10)))
                .andExpect(jsonPath("$[0].leadId", is(50)));

        verify(emailLogService, times(1)).getEmailLogsByCampaignAndLead(campaignId, leadId);
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/campaign/{campaignId}/lead/{leadId} - should return empty when no matching logs")
    void testGetEmailLogsByCampaignAndLeadNotFound() throws Exception {
        // Arrange
        Long campaignId = 999L;
        Long leadId = 999L;
        when(emailLogService.getEmailLogsByCampaignAndLead(campaignId, leadId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/campaign/{campaignId}/lead/{leadId}", campaignId, leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/message/{messageId} - should return log for message")
    void testGetEmailLogByMessage() throws Exception {
        // Arrange
        Long messageId = 12345L;
        List<EmailLog> logs = Arrays.asList(testEmailLog);
        when(emailLogService.getEmailEventsByMailjetMessageId(messageId)).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/message/{messageId}", messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].mailjetMessageId", is(12345)));

        verify(emailLogService, times(1)).getEmailEventsByMailjetMessageId(messageId);
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/message/{messageId} - should return empty for unknown message")
    void testGetEmailLogByMessageNotFound() throws Exception {
        // Arrange
        Long messageId = 999999L;
        when(emailLogService.getEmailEventsByMailjetMessageId(messageId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/message/{messageId}", messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/all - should handle service exceptions gracefully")
    void testGetAllEmailLogsServiceError() throws Exception {
        // Arrange
        when(emailLogService.getAllEmailLogs()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/campaign/{campaignId} - should handle invalid campaign ID")
    void testGetEmailLogsByCampaignInvalidId() throws Exception {
        // Arrange - negative campaign ID
        Long campaignId = -1L;

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/campaign/{campaignId}", campaignId))
                .andExpect(status().isOk()); // May return empty or 400 depending on validation
    }

    @Test
    @DisplayName("Multiple email logs should be returned in correct order")
    void testMultipleEmailLogsOrder() throws Exception {
        // Arrange
        EmailLog log1 = createEmailLog(1L, 12345L, 50L);
        EmailLog log2 = createEmailLog(2L, 12346L, 50L);
        EmailLog log3 = createEmailLog(3L, 12347L, 50L);
        
        List<EmailLog> logs = Arrays.asList(log1, log2, log3);
        when(emailLogService.getEmailLogsByLead(50L)).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/lead/{leadId}", 50L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[2].id", is(3)));
    }

    @Test
    @DisplayName("Email log response should contain all required fields")
    void testEmailLogResponseFields() throws Exception {
        // Arrange
        List<EmailLog> logs = Arrays.asList(testEmailLog);
        when(emailLogService.getAllEmailLogs()).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].mailjetMessageId", notNullValue()))
                .andExpect(jsonPath("$[0].leadId", notNullValue()))
                .andExpect(jsonPath("$[0].campaignId", notNullValue()))
                .andExpect(jsonPath("$[0].emailAddress", notNullValue()))
                .andExpect(jsonPath("$[0].eventType", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/campaign/{campaignId} should validate campaign ID type")
    void testCampaignIdTypeValidation() throws Exception {
        // Arrange - non-numeric campaign ID
        
        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/campaign/invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/lead/{leadId} should validate lead ID type")
    void testLeadIdTypeValidation() throws Exception {
        // Arrange - non-numeric lead ID
        
        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/lead/invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v3/email-logs/message/{messageId} should validate message ID type")
    void testMessageIdTypeValidation() throws Exception {
        // Arrange - non-numeric message ID
        
        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/message/invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Large result sets should be returned completely")
    void testLargeResultSet() throws Exception {
        // Arrange - 1000 email logs
        List<EmailLog> largeLogs = new java.util.ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            largeLogs.add(createEmailLog((long) i, 10000L + i, 50L));
        }
        when(emailLogService.getEmailLogsByLead(50L)).thenReturn(largeLogs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/lead/{leadId}", 50L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1000)));
    }

    @Test
    @DisplayName("Cross-filter with campaign and lead should work correctly")
    void testCrossFilterAccuracy() throws Exception {
        // Arrange
        Long campaignId = 10L;
        Long leadId = 50L;
        
        EmailLog log = createEmailLog(1L, 12345L, 50L);
        log.setCampaignId(campaignId);
        
        List<EmailLog> logs = Arrays.asList(log);
        when(emailLogService.getEmailLogsByCampaignAndLead(campaignId, leadId)).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/v3/email-logs/campaign/{campaignId}/lead/{leadId}", campaignId, leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].campaignId", is(campaignId.intValue())))
                .andExpect(jsonPath("$[0].leadId", is(leadId.intValue())));
    }

    // Helper method to create test email logs
    private EmailLog createEmailLog(Long id, Long messageId, Long leadId) {
        EmailLog log = new EmailLog();
        log.setId(id);
        log.setMailjetMessageId(messageId);
        log.setLeadId(leadId);
        log.setCampaignId(10L);
        log.setEventType(MailjetEventType.SENT);
        log.setEmailAddress("test" + id + "@example.com");
        return log;
    }
}
