package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.entities.EmailTemplate;
import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.EmailTemplateRepository;
import com.operimus.Marketing.security.TestSecurityConfig;
import com.operimus.Marketing.utils.JsonUtils;
import com.operimus.Marketing.services.HubSpotLeadService;
import com.operimus.Marketing.services.MarketingIndexerService;

// Spring AI imports to prevent context crashes
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.embedding.EmbeddingModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// If Spring Boot 3.4+, use MockitoBean. If older, keep MockBean.
import org.springframework.test.context.bean.override.mockito.MockitoBean; 

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MarketingApplication.class)
@AutoConfigureMockMvc(addFilters = false) // Bypasses Security Filters
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Uses the H2 config from application-test.properties
@TestPropertySource(properties = "api.version=v3")
@Import(TestSecurityConfig.class)
public class EmailTemplateControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    // --- MOCKS TO PREVENT CONTEXT CRASHES ---
    
    @MockitoBean // Use @MockBean if Spring Boot < 3.4
    private MarketingIndexerService marketingIndexerService;

    @MockitoBean // Helper to stop PgVector from trying to connect
    private VectorStore vectorStore;

    @MockitoBean // Helper to stop Spring AI from looking for Google Gemini
    private EmbeddingModel embeddingModel;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    // ----------------------------------------

    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void setUp() {
        // Clear DB to ensure clean state for every test
        campaignRepository.deleteAll();
        emailTemplateRepository.deleteAll();
    }

    /**
     * Helper to create a fully populated template to avoid NULL errors
     */
    private EmailTemplate createValidTemplate(String name, String subject) {
        EmailTemplate template = new EmailTemplate();
        template.setName(name);
        template.setDescription("Integration Test Description");
        template.setSubject(subject);
        template.setBody("<h1>Hello World</h1>");
        // Ensure this matches your Enum definition. 
        // If it's a String, this works. If Enum, use TemplateType.EMAIL
        // template.setType(TemplateType.EMAIL); 
        return template;
    }

    @Test
    void givenTemplates_whenGetAllTemplates_thenStatus200() throws Exception {
        // Given
        emailTemplateRepository.saveAndFlush(createValidTemplate("Welcome Template", "Welcome {{firstName}}"));
        emailTemplateRepository.saveAndFlush(createValidTemplate("Promo Template", "Special Offer"));

        // When & Then
        mockMvc.perform(get("/api/" + apiVersion + "/email-templates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Welcome Template")))
                .andExpect(jsonPath("$[1].name", is("Promo Template")));
    }

    @Test
    void givenTemplate_whenGetTemplateById_thenStatus200() throws Exception {
        // Given
        EmailTemplate saved = emailTemplateRepository.saveAndFlush(createValidTemplate("Test Template", "Test Subject"));

        // When & Then
        mockMvc.perform(get("/api/" + apiVersion + "/email-templates/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Test Template")))
                .andExpect(jsonPath("$.subject", is("Test Subject")));
    }

    @Test
    void givenInvalidId_whenGetTemplateById_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/email-templates/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidTemplate_whenCreateTemplate_thenStatus201() throws Exception {
        // Given
        EmailTemplate newTemplate = createValidTemplate("New Template", "New Subject");

        // When & Then
        mockMvc.perform(post("/api/" + apiVersion + "/email-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(newTemplate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Template")))
                .andExpect(jsonPath("$.subject", is("New Subject")));

        assertThat(emailTemplateRepository.findAll()).hasSize(1);
    }

    @Test
    void givenTemplate_whenUpdateTemplate_thenStatus200() throws Exception {
        // Given
        EmailTemplate saved = emailTemplateRepository.saveAndFlush(createValidTemplate("Original", "Old Subject"));

        // Update Payload
        EmailTemplate updatedDTO = new EmailTemplate();
        updatedDTO.setName("Updated Name");
        updatedDTO.setDescription("New desc");
        updatedDTO.setSubject("New Subject");
        // We explicitly don't set Body to test partial updates or full overwrite 
        // (depending on controller logic)

        // When & Then
        mockMvc.perform(put("/api/" + apiVersion + "/email-templates/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.subject", is("New Subject")));

        // Verify DB
        EmailTemplate persisted = emailTemplateRepository.findById(saved.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("Updated Name");
    }

    @Test
    void givenInvalidId_whenUpdateTemplate_thenStatus404() throws Exception {
        EmailTemplate updated = createValidTemplate("Should Fail", "Fail");

        mockMvc.perform(put("/api/" + apiVersion + "/email-templates/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updated)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenTemplate_whenDeleteTemplate_thenStatus204() throws Exception {
        // Given
        EmailTemplate saved = emailTemplateRepository.saveAndFlush(createValidTemplate("To Delete", "Sub"));

        // When & Then
        mockMvc.perform(delete("/api/" + apiVersion + "/email-templates/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(emailTemplateRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void givenValidId_whenTestEmailTemplate_thenStatus200() throws Exception {
        // Given
        EmailTemplate saved = emailTemplateRepository.saveAndFlush(createValidTemplate("Test Send", "Hello {{firstName}}"));

        // When & Then
        mockMvc.perform(post("/api/" + apiVersion + "/email-templates/" + saved.getId() + "/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"testEmail\": \"test@example.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void givenInvalidId_whenTestEmailTemplate_thenStatus404() throws Exception {
        mockMvc.perform(post("/api/" + apiVersion + "/email-templates/99999/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"testEmail\": \"test@example.com\"}"))
                .andExpect(status().isNotFound());
    }
}