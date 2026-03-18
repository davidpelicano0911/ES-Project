package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.repositories.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private HubSpotLeadService hubSpotLeadService;

    @InjectMocks
    private LeadService leadService;

    private Lead lead1;
    private Lead lead2;

    @BeforeEach
    void setUp() {
        lead1 = new Lead();
        lead1.setId(1L);
        lead1.setFirstName("John");
        lead1.setLastName("Doe");
        lead1.setEmail("john.doe@example.com");
        lead1.setPhoneNumber("+1234567890");

        lead2 = new Lead();
        lead2.setId(2L);
        lead2.setFirstName("Alice");
        lead2.setLastName("Smith");
        lead2.setEmail("alice@example.com");
        lead2.setPhoneNumber("+9876543210");

        List<Lead> leads = Arrays.asList(lead1, lead2);

        when(leadRepository.findAll()).thenReturn(leads);
        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead1));
        when(leadRepository.findById(2L)).thenReturn(Optional.of(lead2));
        when(leadRepository.findById(99L)).thenReturn(Optional.empty());
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(leadRepository.existsById(1L)).thenReturn(true);
        when(leadRepository.existsById(2L)).thenReturn(true);
        when(leadRepository.existsById(99L)).thenReturn(false);
    }

    @Test
    void whenCreateLead_thenReturnSavedLead() {
        Lead newLead = new Lead();
        newLead.setFirstName("Bob");
        newLead.setLastName("Brown");
        newLead.setEmail("bob@example.com");

        Lead saved = leadService.createLead(newLead);

        assertThat(saved).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Bob");
        verify(leadRepository, times(2)).save(newLead);
    }

    @Test
    void whenGetAllLeads_thenReturnListOfLeads() {
        List<Lead> result = leadService.getAllLeads();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(Lead::getFirstName).containsExactlyInAnyOrder("John", "Alice");

        verify(leadRepository, times(1)).findAll();
    }

    @Test
    void whenGetLeadByValidId_thenReturnLead() {
        Lead found = leadService.getLead(1L);

        assertThat(found).isNotNull();
        assertThat(found.getFirstName()).isEqualTo("John");

        verify(leadRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetLeadByInvalidId_thenThrowNotFoundException() {
        assertThatThrownBy(() -> leadService.getLead(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
            .hasMessageContaining("Lead not found with id: 99");

        verify(leadRepository, times(1)).findById(99L);
    }

    @Test
    void whenUpdateLead_thenReturnUpdatedLead() {
        Lead updateData = new Lead();
        updateData.setFirstName("JohnUpdated");
        updateData.setLastName("DoeUpdated");
        updateData.setEmail("john.updated@example.com");

        when(leadRepository.save(argThat(l ->
            "JohnUpdated".equals(l.getFirstName()) &&
            l.getId() != null &&
            l.getId().equals(1L)
        ))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead result = leadService.updateLead(1L, updateData);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("JohnUpdated");
        assertThat(result.getLastName()).isEqualTo("DoeUpdated");
        assertThat(result.getEmail()).isEqualTo("john.updated@example.com");

        verify(leadRepository, times(1)).findById(1L);
        verify(leadRepository, times(2)).save(argThat(l ->
            l.getId() != null && l.getId().equals(1L) && "JohnUpdated".equals(l.getFirstName())
        ));
    }

    @Test
    void whenUpdateNonExistingLead_thenThrowNotFoundException() {
        Lead updateData = new Lead();
        updateData.setFirstName("Nobody");

        assertThatThrownBy(() -> leadService.updateLead(99L, updateData))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
            .hasMessageContaining("Lead not found with id: 99");

        verify(leadRepository, times(1)).findById(99L);
        verify(leadRepository, never()).save(any());
    }

    @Test
    void whenCreateLeadWithDuplicateEmail_thenThrowBadRequestException() {
        Lead newLead = new Lead();
        newLead.setFirstName("Bob");
        newLead.setLastName("Brown");
        newLead.setEmail("john.doe@example.com"); // Email already exists
        
        when(leadRepository.existsByEmail("john.doe@example.com")).thenReturn(true);
        
        assertThatThrownBy(() -> leadService.createLead(newLead))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
            .hasMessageContaining("A lead with this email already exists");
        
        verify(leadRepository, times(1)).existsByEmail("john.doe@example.com");
        verify(leadRepository, never()).save(any());
    }

    @Test
    void whenCreateLeadWithDuplicatePhoneNumber_thenThrowBadRequestException() {
        Lead newLead = new Lead();
        newLead.setFirstName("Bob");
        newLead.setLastName("Brown");
        newLead.setEmail("bob@example.com");
        newLead.setPhoneNumber("+1234567890"); // Phone number already exists
        
        when(leadRepository.existsByPhoneNumber("+1234567890")).thenReturn(true);
        
        assertThatThrownBy(() -> leadService.createLead(newLead))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
            .hasMessageContaining("A lead with this phone number already exists");
        
        verify(leadRepository, times(1)).existsByEmail("bob@example.com");
        verify(leadRepository, times(1)).existsByPhoneNumber("+1234567890");
        verify(leadRepository, never()).save(any());
    }

    @Test
    void whenUpdateLeadWithDuplicatePhoneNumber_thenThrowBadRequestException() {
        lead1.setPhoneNumber("+1234567890");
        
        Lead updateData = new Lead();
        updateData.setFirstName("John");
        updateData.setLastName("Doe");
        updateData.setEmail("john.doe@example.com");
        updateData.setPhoneNumber("+9876543210"); // Phone number already exists
        
        when(leadRepository.existsByPhoneNumber("+9876543210")).thenReturn(true);
        
        assertThatThrownBy(() -> leadService.updateLead(1L, updateData))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
            .hasMessageContaining("A lead with this phone number already exists");
        
        verify(leadRepository, times(1)).findById(1L);
        verify(leadRepository, times(1)).existsByPhoneNumber("+9876543210");
        verify(leadRepository, never()).save(any());
    }

    @Test
    void whenDeleteLead_thenRepositoryDeleteByIdIsCalled() {
        doNothing().when(leadRepository).deleteById(1L);

        leadService.deleteLead(1L);

        verify(leadRepository, times(1)).deleteById(1L);
    }

    @Test
    void whenDeleteNonExistingLead_thenThrowNotFoundException() {
        assertThatThrownBy(() -> leadService.deleteLead(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
            .hasMessageContaining("Lead not found with id: 99");

        verify(leadRepository, times(1)).existsById(99L);
        verify(leadRepository, never()).deleteById(99L);
    }

    @Test
    void whenSyncAllFromHubSpot_thenCorrectCountsReturned() {
        Map<String, Object> hubspotLead1 = Map.of(
                "id", "CRM123",
                "properties", Map.of(
                        "email", "new1@example.com",
                        "firstname", "New",
                        "lastname", "User",
                        "phone", "+111111111",
                        "country", "PT",
                        "score", "10",
                        "lastmodifieddate", Instant.now().toString()
                )
        );

        Map<String, Object> hubspotLead2 = Map.of(
                "id", "CRM999",
                "properties", Map.of(
                        "email", "john.doe@example.com",
                        "firstname", "JohnUpdated",
                        "lastname", "DoeUpdated",
                        "phone", "+222222222",
                        "country", "BR",
                        "score", "30",
                        "lastmodifieddate", Instant.now().toString()
                )
        );

        when(hubSpotLeadService.fetchHubSpotContacts())
                .thenReturn(List.of(hubspotLead1, hubspotLead2));

        when(leadRepository.findByCrmId("CRM123")).thenReturn(Optional.empty());
        when(leadRepository.findByCrmId("CRM999")).thenReturn(Optional.empty());

        when(leadRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(lead1));
        when(leadRepository.findByEmail("new1@example.com")).thenReturn(Optional.empty());

        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = leadService.syncAllFromHubSpot();

        assertThat(result.get("created")).isEqualTo(1);
        assertThat(result.get("updated")).isEqualTo(1);
        assertThat(result.get("lastSyncedAt")).isNotNull();

        verify(leadRepository, times(2)).save(any(Lead.class));
    }

    @Test
    void whenGetSyncStatus_thenPendingCountsAreCalculatedCorrectly() {
        Date latestSync = new Date(System.currentTimeMillis() - 10000);

        Map<String, Object> hubspotNewLead = Map.of(
                "id", "CRM_NEW",
                "properties", Map.of(
                        "email", "newlead@example.com",
                        "lastmodifieddate", Instant.now().toString()
                )
        );

        Map<String, Object> hubspotUpdatedLead = Map.of(
                "id", "CRM_EXIST",
                "properties", Map.of(
                        "email", "john.doe@example.com",
                        "lastmodifieddate", Instant.now().toString()
                )
        );

        when(hubSpotLeadService.fetchHubSpotContacts())
                .thenReturn(List.of(hubspotNewLead, hubspotUpdatedLead));

        when(leadRepository.findByCrmId("CRM_NEW")).thenReturn(Optional.empty());
        when(leadRepository.findByEmail("newlead@example.com")).thenReturn(Optional.empty());

        lead1.setLastSyncedAt(new Date(System.currentTimeMillis() - 20000));

        when(leadRepository.findByCrmId("CRM_EXIST")).thenReturn(Optional.empty());
        when(leadRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(lead1));

        when(leadRepository.findLatestSyncDate()).thenReturn(latestSync);

        var status = leadService.getSyncStatus();

        assertThat(status.pendingCreates).isEqualTo(1);
        assertThat(status.pendingUpdates).isEqualTo(1);
        assertThat(status.updatesAvailable).isTrue();
        assertThat(status.lastSyncedAt).isEqualTo(latestSync);
    }
}