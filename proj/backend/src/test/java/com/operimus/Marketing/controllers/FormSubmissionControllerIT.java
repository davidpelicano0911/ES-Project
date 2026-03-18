package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.entities.FormSubmission;
import com.operimus.Marketing.repositories.FormSubmissionRepository;
import com.operimus.Marketing.security.TestSecurityConfig;
import com.operimus.Marketing.utils.JsonUtils;
import com.operimus.Marketing.services.HubSpotLeadService;
import com.operimus.Marketing.services.MarketingIndexerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.operimus.Marketing.services.MarketingIndexerService;

@SpringBootTest(classes = {MarketingApplication.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@TestPropertySource(properties = "api.version=v1")
@Import(TestSecurityConfig.class)
public class FormSubmissionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FormSubmissionRepository submissionRepository;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;
        
    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void resetDb() {
        submissionRepository.deleteAll();
    }

    @Test
    void givenFormSubmissions_whenGetAllSubmissions_thenStatus200() throws Exception {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{\"name\":\"John Doe\",\"email\":\"john@example.com\"}");
        submissionRepository.saveAndFlush(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{\"name\":\"Jane Smith\",\"email\":\"jane@example.com\"}");
        submissionRepository.saveAndFlush(submission2);

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].formId", is(1)))
                .andExpect(jsonPath("$[0].leadId", is(101)))
                .andExpect(jsonPath("$[1].formId", is(2)))
                .andExpect(jsonPath("$[1].leadId", is(102)));
    }

    @Test
    void givenEmptyDatabase_whenGetAllSubmissions_thenStatus200_andEmptyList() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenFormId_whenGetSubmissionsByFormId_thenStatus200_andFilteredResults() throws Exception {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{\"name\":\"John\"}");
        submissionRepository.saveAndFlush(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{\"name\":\"Jane\"}");
        submissionRepository.saveAndFlush(submission2);

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?formId=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].formId", is(1)))
                .andExpect(jsonPath("$[0].leadId", is(101)));
    }

    @Test
    void givenCampaignId_whenGetSubmissionsByCampaignId_thenStatus200_andFilteredResults() throws Exception {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setCampaignId(201L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{\"name\":\"John\"}");
        submissionRepository.saveAndFlush(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setCampaignId(202L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{\"name\":\"Jane\"}");
        submissionRepository.saveAndFlush(submission2);

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?campaignId=201"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].campaignId", is(201)))
                .andExpect(jsonPath("$[0].leadId", is(101)));
    }

    @Test
    void givenLeadId_whenGetSubmissionsByLeadId_thenStatus200_andFilteredResults() throws Exception {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{\"name\":\"John\"}");
        submissionRepository.saveAndFlush(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{\"name\":\"Jane\"}");
        submissionRepository.saveAndFlush(submission2);

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?leadId=102"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].leadId", is(102)))
                .andExpect(jsonPath("$[0].formId", is(2)));
    }

    @Test
    void givenValidFormSubmission_whenCreateSubmission_thenStatus201() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 1L);
        submissionBody.put("leadId", 101L);
        submissionBody.put("name", "John Doe");
        submissionBody.put("email", "john@example.com");

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.formId", is(1)))
                .andExpect(jsonPath("$.leadId", is(101)))
                .andExpect(jsonPath("$.submittedAt", notNullValue()));

        assertThat(submissionRepository.findAll()).hasSize(1);
    }

    @Test
    void givenValidSubmission_whenCreateSubmission_thenDataPersisted() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 2L);
        submissionBody.put("campaignId", 201L);
        submissionBody.put("leadId", 102L);
        submissionBody.put("name", "Jane Smith");
        submissionBody.put("email", "jane@example.com");
        submissionBody.put("message", "Interested in your product");

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        assertThat(submissionRepository.findAll()).hasSize(1);
        FormSubmission saved = submissionRepository.findAll().get(0);
        assertThat(saved.getFormId()).isEqualTo(2L);
        assertThat(saved.getCampaignId()).isEqualTo(201L);
        assertThat(saved.getLeadId()).isEqualTo(102L);
        assertThat(saved.getSubmittedAt()).isNotNull();
    }

    @Test
    void givenMinimalSubmission_whenCreateSubmission_thenStatus201() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 1L);

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.formId", is(1)))
                .andExpect(jsonPath("$.submittedAt", notNullValue()));
    }

    @Test
    void givenEmptyBody_whenCreateSubmission_thenStatus201() throws Exception {
        // Empty body is allowed - the service can handle requests with no data
        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void givenNullBody_whenCreateSubmission_thenStatus400() throws Exception {
        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenMultipleSubmissions_whenFilterByFormAndCampaignId_thenStatus200_andCorrectFiltering() throws Exception {
        // Create submissions with different combinations
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setCampaignId(201L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{\"field\":\"value1\"}");
        submissionRepository.saveAndFlush(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(1L);
        submission2.setCampaignId(202L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{\"field\":\"value2\"}");
        submissionRepository.saveAndFlush(submission2);

        FormSubmission submission3 = new FormSubmission();
        submission3.setFormId(2L);
        submission3.setCampaignId(201L);
        submission3.setLeadId(103L);
        submission3.setResponsesJson("{\"field\":\"value3\"}");
        submissionRepository.saveAndFlush(submission3);

        // Filter by formId only
        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?formId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Filter by campaignId only
        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?campaignId=201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Filter by leadId only
        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?leadId=102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].formId", is(1)));
    }

    @Test
    void givenSubmissionWithComplexResponseJson_whenCreateSubmission_thenResponsesJsonPreserved() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 1L);
        submissionBody.put("leadId", 101L);
        
        Map<String, Object> responses = new HashMap<>();
        responses.put("name", "John Doe");
        responses.put("email", "john@example.com");
        responses.put("preferences", new String[]{"email", "sms"});
        submissionBody.putAll(responses);

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.responsesJson", notNullValue()));

        assertThat(submissionRepository.findAll()).hasSize(1);
    }

    @Test
    void givenSubmission_whenCreated_thenSubmittedAtTimestampIsSet() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 1L);
        submissionBody.put("leadId", 101L);

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submittedAt").exists())
                .andExpect(jsonPath("$.submittedAt").isString());

        FormSubmission saved = submissionRepository.findAll().get(0);
        assertThat(saved.getSubmittedAt()).isNotNull();
    }

    @Test
    void givenInvalidParameterValue_whenGetSubmissions_thenHandledGracefully() throws Exception {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{}");
        submissionRepository.saveAndFlush(submission);

        // Try with non-existent form ID - should return empty list
        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?formId=999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenMultipleSubmissionsSameForm_whenListSubmissions_thenAllReturned() throws Exception {
        for (int i = 1; i <= 5; i++) {
            FormSubmission submission = new FormSubmission();
            submission.setFormId(1L);
            submission.setLeadId((long) (100 + i));
            submission.setResponsesJson("{\"index\":" + i + "}");
            submissionRepository.saveAndFlush(submission);
        }

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?formId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void givenSubmission_whenRetrievedById_thenContainsAllFields() throws Exception {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setCampaignId(201L);
        submission.setResponsesJson("{\"name\":\"John Doe\"}");
        FormSubmission saved = submissionRepository.saveAndFlush(submission);

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$[0].formId", is(1)))
                .andExpect(jsonPath("$[0].leadId", is(101)))
                .andExpect(jsonPath("$[0].campaignId", is(201)))
                .andExpect(jsonPath("$[0].responsesJson", notNullValue()));
    }

    @Test
    void givenNoFilterParameters_whenGetSubmissions_thenReturnAll() throws Exception {
        for (int i = 1; i <= 3; i++) {
            FormSubmission submission = new FormSubmission();
            submission.setFormId((long) i);
            submission.setLeadId((long) (100 + i));
            submission.setResponsesJson("{}");
            submissionRepository.saveAndFlush(submission);
        }

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void givenSubmissionWithoutCampaignId_whenCreate_thenCampaignIdIsNull() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 1L);
        submissionBody.put("leadId", 101L);

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.campaignId").doesNotExist());
    }

    @Test
    void givenMultipleFilterAttempts_whenOnlyOneMatches_thenReturnOne() throws Exception {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");
        submissionRepository.saveAndFlush(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{}");
        submissionRepository.saveAndFlush(submission2);

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?formId=1&leadId=102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].formId", is(1)));
    }

    @Test
    void givenLargeFormId_whenCreate_thenAccepted() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", Long.MAX_VALUE);
        submissionBody.put("leadId", 101L);

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formId", is(Long.MAX_VALUE)));
    }

    @Test
    void givenZeroFormId_whenCreate_thenAccepted() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 0L);
        submissionBody.put("leadId", 101L);

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formId", is(0)));
    }

    @Test
    void givenValidSubmission_whenCreated_thenIdIsGenerated() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 1L);
        submissionBody.put("leadId", 101L);

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void givenMultipleSubmissionsWithSameLead_whenFilterByLead_thenReturnAll() throws Exception {
        for (int i = 1; i <= 3; i++) {
            FormSubmission submission = new FormSubmission();
            submission.setFormId((long) i);
            submission.setLeadId(101L);
            submission.setResponsesJson("{}");
            submissionRepository.saveAndFlush(submission);
        }

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?leadId=101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].leadId", everyItem(is(101))));
    }

    @Test
    void givenMultipleSubmissionsWithSameCampaign_whenFilterByCampaign_thenReturnAll() throws Exception {
        for (int i = 1; i <= 3; i++) {
            FormSubmission submission = new FormSubmission();
            submission.setFormId((long) i);
            submission.setCampaignId(201L);
            submission.setLeadId((long) (100 + i));
            submission.setResponsesJson("{}");
            submissionRepository.saveAndFlush(submission);
        }

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?campaignId=201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].campaignId", everyItem(is(201))));
    }

    @Test
    void givenSubmissionWithJsonResponse_whenCreate_thenJsonIsReturned() throws Exception {
        Map<String, Object> submissionBody = new HashMap<>();
        submissionBody.put("formId", 1L);
        submissionBody.put("leadId", 101L);
        submissionBody.put("fieldA", "valueA");
        submissionBody.put("fieldB", "valueB");

        mockMvc.perform(post("/api/" + apiVersion + "/form-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(submissionBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsesJson").isString());
    }

    @Test
    void givenInvalidFilter_whenGetSubmissions_thenReturnEmptyList() throws Exception {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{}");
        submissionRepository.saveAndFlush(submission);

        mockMvc.perform(get("/api/" + apiVersion + "/form-submissions?formId=9999&campaignId=9999&leadId=9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
