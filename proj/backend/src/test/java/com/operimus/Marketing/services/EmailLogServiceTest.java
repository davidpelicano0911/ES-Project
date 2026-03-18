package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.EmailLog;
import com.operimus.Marketing.entities.MailjetEventType;
import com.operimus.Marketing.repositories.EmailLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailLogService
 * 
 * Tests the email logging functionality including:
 * - Initial SENT event logging
 * - Event deduplication
 * - Email log retrieval by campaign/lead/message
 * - Analytics calculations
 */
@DisplayName("EmailLogService Tests")
class EmailLogServiceTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @InjectMocks
    private EmailLogService emailLogService;

    private EmailLog sentRecord;
    private EmailLog openedEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test SENT record
        sentRecord = new EmailLog();
        sentRecord.setId(1L);
        sentRecord.setCampaignId(100L);
        sentRecord.setLeadId(50L);
        sentRecord.setEmailAddress("test@example.com");
        sentRecord.setMailjetMessageId(12345L);
        sentRecord.setEventType(MailjetEventType.SENT);

        // Create test OPENED event
        openedEvent = new EmailLog();
        openedEvent.setId(2L);
        openedEvent.setMailjetMessageId(12345L);
        openedEvent.setEventType(MailjetEventType.OPENED);
        openedEvent.setLeadId(50L);
    }

    @Test
    @DisplayName("Should log initial email send event")
    void testLogEmail() {
        // Arrange
        when(emailLogRepository.save(any(EmailLog.class))).thenReturn(sentRecord);

        // Act
        EmailLog result = emailLogService.logEmail(100L, 50L, "test@example.com", 12345L, "Test Subject");

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getCampaignId());
        assertEquals(50L, result.getLeadId());
        assertEquals("test@example.com", result.getEmailAddress());
        assertEquals(12345L, result.getMailjetMessageId());
        assertEquals(MailjetEventType.SENT, result.getEventType());
        verify(emailLogRepository, times(1)).save(any(EmailLog.class));
    }

    @Test
    @DisplayName("Should log event when SENT record exists with valid leadId")
    void testLogEventWithValidLeadId() {
        // Arrange
        when(emailLogRepository.findByMailjetMessageIdAndEventType(12345L, MailjetEventType.SENT))
                .thenReturn(Optional.of(sentRecord));
        when(emailLogRepository.save(any(EmailLog.class))).thenReturn(openedEvent);

        // Act
        EmailLog result = emailLogService.logEvent(12345L, MailjetEventType.OPENED);

        // Assert
        assertNotNull(result);
        assertEquals(MailjetEventType.OPENED, result.getEventType());
        assertEquals(50L, result.getLeadId());
        verify(emailLogRepository, times(1)).save(any(EmailLog.class));
    }

    @Test
    @DisplayName("Should NOT log event when SENT record doesn't exist")
    void testLogEventWithoutSentRecord() {
        // Arrange
        when(emailLogRepository.findByMailjetMessageIdAndEventType(99999L, MailjetEventType.SENT))
                .thenReturn(Optional.empty());

        // Act
        EmailLog result = emailLogService.logEvent(99999L, MailjetEventType.OPENED);

        // Assert
        assertNull(result);
        verify(emailLogRepository, never()).save(any(EmailLog.class));
    }

    @Test
    @DisplayName("Should NOT log event when SENT record has no leadId")
    void testLogEventWithoutLeadId() {
        // Arrange
        EmailLog sentWithoutLead = new EmailLog();
        sentWithoutLead.setMailjetMessageId(12345L);
        sentWithoutLead.setEventType(MailjetEventType.SENT);
        sentWithoutLead.setLeadId(null);

        when(emailLogRepository.findByMailjetMessageIdAndEventType(12345L, MailjetEventType.SENT))
                .thenReturn(Optional.of(sentWithoutLead));

        // Act
        EmailLog result = emailLogService.logEvent(12345L, MailjetEventType.OPENED);

        // Assert
        assertNull(result);
        verify(emailLogRepository, never()).save(any(EmailLog.class));
    }

    @Test
    @DisplayName("Should retrieve email logs by campaign")
    void testGetEmailLogsByCampaign() {
        // Arrange
        List<EmailLog> expectedLogs = Arrays.asList(sentRecord, openedEvent);
        when(emailLogRepository.findByCampaignId(100L)).thenReturn(expectedLogs);

        // Act
        List<EmailLog> result = emailLogService.getEmailLogsByCampaign(100L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(emailLogRepository, times(1)).findByCampaignId(100L);
    }

    @Test
    @DisplayName("Should retrieve email logs by lead")
    void testGetEmailLogsByLead() {
        // Arrange
        List<EmailLog> expectedLogs = Arrays.asList(sentRecord, openedEvent);
        when(emailLogRepository.findByLeadId(50L)).thenReturn(expectedLogs);

        // Act
        List<EmailLog> result = emailLogService.getEmailLogsByLead(50L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(emailLogRepository, times(1)).findByLeadId(50L);
    }

    @Test
    @DisplayName("Should retrieve email logs by campaign and lead")
    void testGetEmailLogsByCampaignAndLead() {
        // Arrange
        List<EmailLog> expectedLogs = Arrays.asList(sentRecord, openedEvent);
        when(emailLogRepository.findByCampaignIdAndLeadId(100L, 50L)).thenReturn(expectedLogs);

        // Act
        List<EmailLog> result = emailLogService.getEmailLogsByCampaignAndLead(100L, 50L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(emailLogRepository, times(1)).findByCampaignIdAndLeadId(100L, 50L);
    }

    @Test
    @DisplayName("Should retrieve all events by message ID")
    void testGetEmailEventsByMailjetMessageId() {
        // Arrange
        List<EmailLog> expectedEvents = Arrays.asList(sentRecord, openedEvent);
        when(emailLogRepository.findByMailjetMessageId(12345L)).thenReturn(expectedEvents);

        // Act
        List<EmailLog> result = emailLogService.getEmailEventsByMailjetMessageId(12345L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(emailLogRepository, times(1)).findByMailjetMessageId(12345L);
    }

    @Test
    @DisplayName("Should retrieve SENT record by message ID")
    void testGetEmailSendByMailjetMessageId() {
        // Arrange
        when(emailLogRepository.findByMailjetMessageIdAndEventType(12345L, MailjetEventType.SENT))
                .thenReturn(Optional.of(sentRecord));

        // Act
        Optional<EmailLog> result = emailLogService.getEmailSendByMailjetMessageId(12345L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(sentRecord, result.get());
        verify(emailLogRepository, times(1)).findByMailjetMessageIdAndEventType(12345L, MailjetEventType.SENT);
    }

    @Test
    @DisplayName("Should check if event exists (deduplication)")
    void testEventExists() {
        // Arrange
        when(emailLogRepository.existsByMailjetMessageIdAndEventType(12345L, MailjetEventType.OPENED))
                .thenReturn(true);

        // Act
        boolean result = emailLogService.eventExists(12345L, MailjetEventType.OPENED);

        // Assert
        assertTrue(result);
        verify(emailLogRepository, times(1)).existsByMailjetMessageIdAndEventType(12345L, MailjetEventType.OPENED);
    }

    @Test
    @DisplayName("Should return false when event doesn't exist")
    void testEventDoesNotExist() {
        // Arrange
        when(emailLogRepository.existsByMailjetMessageIdAndEventType(12345L, MailjetEventType.CLICKED))
                .thenReturn(false);

        // Act
        boolean result = emailLogService.eventExists(12345L, MailjetEventType.CLICKED);

        // Assert
        assertFalse(result);
        verify(emailLogRepository, times(1)).existsByMailjetMessageIdAndEventType(12345L, MailjetEventType.CLICKED);
    }

    @Test
    @DisplayName("Should retrieve all email logs")
    void testGetAllEmailLogs() {
        // Arrange
        List<EmailLog> expectedLogs = Arrays.asList(sentRecord, openedEvent);
        when(emailLogRepository.findAll()).thenReturn(expectedLogs);

        // Act
        List<EmailLog> result = emailLogService.getAllEmailLogs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(emailLogRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should count emails in campaign")
    void testCountEmailsInCampaign() {
        // Arrange
        when(emailLogRepository.countByCampaignIdAndEventType(100L, MailjetEventType.SENT))
                .thenReturn(10L);

        // Act
        long result = emailLogService.countEmailsInCampaign(100L);

        // Assert
        assertEquals(10L, result);
        verify(emailLogRepository, times(1)).countByCampaignIdAndEventType(100L, MailjetEventType.SENT);
    }

    @Test
    @DisplayName("Should calculate open rate for campaign")
    void testGetOpenRateForCampaign() {
        // Arrange
        when(emailLogRepository.countByCampaignIdAndEventType(100L, MailjetEventType.SENT))
                .thenReturn(100L);
        when(emailLogRepository.countByCampaignIdAndEventTypeIn(eq(100L), anyList()))
                .thenReturn(25L);

        // Act
        double result = emailLogService.getOpenRateForCampaign(100L);

        // Assert
        assertEquals(25.0, result);
    }

    @Test
    @DisplayName("Should return 0 open rate when no emails sent")
    void testGetOpenRateForCampaignWithZeroEmails() {
        // Arrange
        when(emailLogRepository.countByCampaignIdAndEventType(100L, MailjetEventType.SENT))
                .thenReturn(0L);

        // Act
        double result = emailLogService.getOpenRateForCampaign(100L);

        // Assert
        assertEquals(0.0, result);
    }
}
