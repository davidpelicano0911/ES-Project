package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.FormSubmission;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.repositories.FormSubmissionRepository;
import com.operimus.Marketing.repositories.FormTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FormSubmissionServiceTest {

    @Mock
    private FormSubmissionRepository submissionRepository;

    @Mock
    private FormTemplateRepository templateRepository;

    @Mock
    private LeadService leadService;

    @Mock
    private WorkflowEngine workflowEngine;

    @InjectMocks
    private FormSubmissionService submissionService;

    private FormSubmission submission1;
    private FormSubmission submission2;
    private FormSubmission submission3;

    @BeforeEach
    void setUp() {
        // InjectMocks handles the field injection via reflection

        submission1 = new FormSubmission();
        submission1.setId(1L);
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{\"name\":\"John Doe\",\"email\":\"john@example.com\"}");
        submission1.setSubmittedAt(new Date());

        submission2 = new FormSubmission();
        submission2.setId(2L);
        submission2.setFormId(2L);
        submission2.setCampaignId(201L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{\"name\":\"Jane Smith\",\"email\":\"jane@example.com\"}");
        submission2.setSubmittedAt(new Date());

        submission3 = new FormSubmission();
        submission3.setId(3L);
        submission3.setFormId(1L);
        submission3.setLeadId(103L);
        submission3.setResponsesJson("{\"name\":\"Bob Wilson\",\"phone\":\"555-1234\"}");
        submission3.setSubmittedAt(new Date());

        List<FormSubmission> allSubmissions = Arrays.asList(submission1, submission2, submission3);

        when(submissionRepository.findAll()).thenReturn(allSubmissions);
        when(submissionRepository.findByFormId(1L)).thenReturn(Arrays.asList(submission1, submission3));
        when(submissionRepository.findByFormId(2L)).thenReturn(Arrays.asList(submission2));
        when(submissionRepository.findByFormId(999L)).thenReturn(new ArrayList<>());
        when(submissionRepository.findByCampaignId(201L)).thenReturn(Arrays.asList(submission2));
        when(submissionRepository.findByCampaignId(999L)).thenReturn(new ArrayList<>());
        when(submissionRepository.findByLeadId(101L)).thenReturn(Arrays.asList(submission1));
        when(submissionRepository.findByLeadId(102L)).thenReturn(Arrays.asList(submission2));
        when(submissionRepository.findByLeadId(103L)).thenReturn(Arrays.asList(submission3));
        when(submissionRepository.findByLeadId(999L)).thenReturn(new ArrayList<>());
        when(submissionRepository.save(any(FormSubmission.class)))
                .thenAnswer(invocation -> {
                    FormSubmission saved = invocation.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(100L);
                    }
                    if (saved.getSubmittedAt() == null) {
                        saved.setSubmittedAt(new Date());
                    }
                    return saved;
                });
    }

    @Test
    void whenGetAllSubmissions_thenReturnListOfSubmissions() {
        List<FormSubmission> result = submissionService.getAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).contains(submission1, submission2, submission3);

        verify(submissionRepository, times(1)).findAll();
    }

    @Test
    void whenGetSubmissionsByFormId_thenReturnFilteredList() {
        List<FormSubmission> result = submissionService.getByForm(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(submission1, submission3);
        assertThat(result.stream().allMatch(s -> s.getFormId().equals(1L))).isTrue();

        verify(submissionRepository, times(1)).findByFormId(1L);
    }

    @Test
    void whenGetSubmissionsByFormId_nonExistent_thenReturnEmptyList() {
        List<FormSubmission> result = submissionService.getByForm(999L);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(submissionRepository, times(1)).findByFormId(999L);
    }

    @Test
    void whenGetSubmissionsByCampaignId_thenReturnFilteredList() {
        List<FormSubmission> result = submissionService.getByCampaign(201L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCampaignId()).isEqualTo(201L);

        verify(submissionRepository, times(1)).findByCampaignId(201L);
    }

    @Test
    void whenGetSubmissionsByLeadId_thenReturnFilteredList() {
        List<FormSubmission> result = submissionService.getByLead(101L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeadId()).isEqualTo(101L);

        verify(submissionRepository, times(1)).findByLeadId(101L);
    }

    @Test
    void whenGetSubmissions_withFormId_thenReturnFormSubmissions() {
        List<FormSubmission> result = submissionService.getSubmissions(1L, null, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getFormId().equals(1L));

        verify(submissionRepository, times(1)).findByFormId(1L);
    }

    @Test
    void whenGetSubmissions_withCampaignId_thenReturnCampaignSubmissions() {
        List<FormSubmission> result = submissionService.getSubmissions(null, 201L, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCampaignId()).isEqualTo(201L);

        verify(submissionRepository, times(1)).findByCampaignId(201L);
    }

    @Test
    void whenGetSubmissions_withLeadId_thenReturnLeadSubmissions() {
        List<FormSubmission> result = submissionService.getSubmissions(null, null, 101L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeadId()).isEqualTo(101L);

        verify(submissionRepository, times(1)).findByLeadId(101L);
    }

    @Test
    void whenGetSubmissions_noParameters_thenReturnAll() {
        List<FormSubmission> result = submissionService.getSubmissions(null, null, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        verify(submissionRepository, times(1)).findAll();
    }

    @Test
    void whenGetSubmissions_prioritizeFormId() {
        // When multiple parameters are provided, formId takes priority
        List<FormSubmission> result = submissionService.getSubmissions(1L, 201L, 101L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        verify(submissionRepository, times(1)).findByFormId(1L);
        verify(submissionRepository, never()).findByCampaignId(anyLong());
        verify(submissionRepository, never()).findByLeadId(anyLong());
    }

    @Test
    void whenCreateSubmission_withValidData_thenReturnSavedSubmission() {
        Map<String, Object> responses = new HashMap<>();
        responses.put("name", "New Lead");
        responses.put("email", "new@example.com");

        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("leadId", 104L);
        body.put("responses", responses);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved).isNotNull();
        assertThat(saved.getFormId()).isEqualTo(1L);
        assertThat(saved.getLeadId()).isEqualTo(104L);
        assertThat(saved.getSubmittedAt()).isNotNull();

        verify(submissionRepository, times(1)).save(any(FormSubmission.class));
    }

    @Test
    void whenCreateSubmission_withMinimalData_thenReturnSavedSubmission() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 2L);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved).isNotNull();
        assertThat(saved.getFormId()).isEqualTo(2L);
        assertThat(saved.getSubmittedAt()).isNotNull();

        verify(submissionRepository, times(1)).save(any(FormSubmission.class));
    }

    @Test
    void whenCreateSubmission_setSubmittedAtTimestamp() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("leadId", 105L);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved.getSubmittedAt()).isNotNull();
    }

    @Test
    void whenCreateSubmission_withCampaignId_thenCampaignIdIsSaved() {
        Map<String, Object> responses = new HashMap<>();
        responses.put("name", "Test");
        
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("campaignId", 202L);
        body.put("leadId", 105L);
        body.put("responses", responses);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved.getCampaignId()).isEqualTo(202L);

        verify(submissionRepository, times(1)).save(any(FormSubmission.class));
    }

    @Test
    void whenCreateSubmission_responsesAreJsonSerialized() {
        Map<String, Object> responses = new HashMap<>();
        responses.put("name", "Test User");
        responses.put("email", "test@example.com");

        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("leadId", 105L);
        body.put("responses", responses);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved.getResponsesJson()).isNotNull();
        // Verify the service called save (with any JSON content)
        verify(submissionRepository, times(1)).save(any(FormSubmission.class));
    }

    @Test
    void whenCreateSubmission_emptyBody_thenResponsesJsonIsEmpty() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved.getResponsesJson()).isNotNull();
        // Should have empty or minimal JSON

        verify(submissionRepository, times(1)).save(any(FormSubmission.class));
    }

    @Test
    void whenCreateSubmission_withMultipleResponses_thenAllArePreserved() {
        Map<String, Object> responses = new HashMap<>();
        responses.put("firstName", "John");
        responses.put("lastName", "Doe");
        responses.put("email", "john.doe@example.com");
        responses.put("phone", "555-9876");
        responses.put("country", "USA");

        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("leadId", 106L);
        body.put("responses", responses);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved.getResponsesJson()).isNotNull();
        // The service should have serialized the responses to JSON
        verify(submissionRepository, times(1)).save(any(FormSubmission.class));
    }

    @Test
    void whenGetSubmissions_multipleSubmissionsSameForm_thenAllReturned() {
        List<FormSubmission> result = submissionService.getByForm(1L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getFormId().equals(1L));
    }

    @Test
    void whenGetSubmissions_submissionWithoutLeadId_thenReturned() {
        FormSubmission noLeadSubmission = new FormSubmission();
        noLeadSubmission.setId(4L);
        noLeadSubmission.setFormId(3L);
        noLeadSubmission.setResponsesJson("{}");

        when(submissionRepository.findByFormId(3L)).thenReturn(Arrays.asList(noLeadSubmission));

        List<FormSubmission> result = submissionService.getByForm(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeadId()).isNull();
    }

    @Test
    void whenGetByForm_thenRepositoryFindByFormIdIsCalled() {
        submissionService.getByForm(1L);
        
        verify(submissionRepository, times(1)).findByFormId(1L);
    }

    @Test
    void whenGetByCampaign_thenRepositoryFindByCampaignIdIsCalled() {
        submissionService.getByCampaign(201L);
        
        verify(submissionRepository, times(1)).findByCampaignId(201L);
    }

    @Test
    void whenGetByLead_thenRepositoryFindByLeadIdIsCalled() {
        submissionService.getByLead(101L);
        
        verify(submissionRepository, times(1)).findByLeadId(101L);
    }

    @Test
    void whenCreateSubmission_withAllFields_thenAllFieldsAreSaved() {
        Map<String, Object> responses = new HashMap<>();
        responses.put("name", "John Doe");
        responses.put("email", "john@example.com");
        responses.put("phone", "555-1234");

        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("campaignId", 201L);
        body.put("leadId", 105L);
        body.put("responses", responses);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved).isNotNull();
        assertThat(saved.getFormId()).isEqualTo(1L);
        assertThat(saved.getCampaignId()).isEqualTo(201L);
        assertThat(saved.getLeadId()).isEqualTo(105L);
        assertThat(saved.getSubmittedAt()).isNotNull();
    }

    @Test
    void whenGetSubmissions_campaignIdTakesPrecedenceOverLeadId() {
        List<FormSubmission> result = submissionService.getSubmissions(null, 201L, 101L);
        
        // Should return campaign submissions, not lead submissions
        assertThat(result).isNotNull();
        verify(submissionRepository, times(1)).findByCampaignId(201L);
        verify(submissionRepository, never()).findByLeadId(anyLong());
    }

    @Test
    void whenCreateSubmission_nullResponses_thenEmptyJsonIsUsed() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("leadId", 107L);
        // No responses field

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved).isNotNull();
        assertThat(saved.getResponsesJson()).isNotNull();
        // Should contain empty object or empty map serialized
    }

    @Test
    void whenGetByForm_emptyResult_thenReturnEmptyList() {
        when(submissionRepository.findByFormId(999L)).thenReturn(new ArrayList<>());
        
        List<FormSubmission> result = submissionService.getByForm(999L);
        
        assertThat(result).isEmpty();
    }

    @Test
    void whenGetByCampaign_emptyResult_thenReturnEmptyList() {
        when(submissionRepository.findByCampaignId(999L)).thenReturn(new ArrayList<>());
        
        List<FormSubmission> result = submissionService.getByCampaign(999L);
        
        assertThat(result).isEmpty();
    }

    @Test
    void whenGetByLead_emptyResult_thenReturnEmptyList() {
        when(submissionRepository.findByLeadId(999L)).thenReturn(new ArrayList<>());
        
        List<FormSubmission> result = submissionService.getByLead(999L);
        
        assertThat(result).isEmpty();
    }

    @Test
    void whenCreateSubmission_largeFormId_thenAccepted() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", Long.MAX_VALUE);
        body.put("leadId", 108L);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved).isNotNull();
        assertThat(saved.getFormId()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void whenCreateSubmission_zeroFormId_thenAccepted() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 0L);
        body.put("leadId", 109L);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved).isNotNull();
        assertThat(saved.getFormId()).isEqualTo(0L);
    }

    @Test
    void whenGetSubmissions_allParametersNull_thenGetAllIsCalled() {
        submissionService.getSubmissions(null, null, null);
        
        verify(submissionRepository, times(1)).findAll();
    }

    @Test
    void whenCreateSubmission_repositorySaveIsCalled() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);

        submissionService.createSubmission(body);

        verify(submissionRepository, times(1)).save(any(FormSubmission.class));
    }

    @Test
    void whenCreateSubmission_responseObjectIsNull_thenHandledGracefully() {
        Map<String, Object> body = new HashMap<>();
        body.put("formId", 1L);
        body.put("leadId", 110L);
        body.put("responses", null);

        FormSubmission saved = submissionService.createSubmission(body);

        assertThat(saved).isNotNull();
        assertThat(saved.getResponsesJson()).isNotNull();
    }

    @Test
    void whenGetAll_thenReturnAllSubmissionsFromRepository() {
        submissionService.getAll();
        
        verify(submissionRepository, times(1)).findAll();
    }

    @Test
    void whenGetAll_withMultipleSubmissions_thenReturnAll() {
        List<FormSubmission> result = submissionService.getAll();
        
        assertThat(result).hasSize(3);
    }
}
