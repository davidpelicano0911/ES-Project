package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.entities.FormTemplate;
import com.operimus.Marketing.repositories.FormTemplateRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
public class FormTemplateControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void resetDb() {
        formTemplateRepository.deleteAll();
    }

    @Test
    void givenFormTemplates_whenGetAllFormTemplates_thenStatus200() throws Exception {
        FormTemplate template1 = new FormTemplate("Lead Form", "Capture leads", 101L, "{\"type\":\"lead\"}");
        template1.setIsPublished(true);
        formTemplateRepository.saveAndFlush(template1);

        FormTemplate template2 = new FormTemplate("Survey Form", "Customer feedback", 102L, "{\"type\":\"survey\"}");
        template2.setIsPublished(false);
        formTemplateRepository.saveAndFlush(template2);

        mockMvc.perform(get("/api/" + apiVersion + "/form-template"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Lead Form")))
                .andExpect(jsonPath("$[0].isPublished", is(true)))
                .andExpect(jsonPath("$[1].name", is("Survey Form")))
                .andExpect(jsonPath("$[1].isPublished", is(false)));
    }

    @Test
    void givenFormTemplate_whenGetFormTemplateById_thenStatus200() throws Exception {
        FormTemplate template = new FormTemplate("Contact Form", "Contact us", 103L, "{\"fields\":[\"name\",\"email\"]}");
        template.setIsPublished(true);
        FormTemplate saved = formTemplateRepository.saveAndFlush(template);

        mockMvc.perform(get("/api/" + apiVersion + "/form-template/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Contact Form")))
                .andExpect(jsonPath("$.description", is("Contact us")))
                .andExpect(jsonPath("$.isPublished", is(true)))
                .andExpect(jsonPath("$.formJson").isString())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test 
    void givenInvalidId_whenGetFormTemplateById_thenStatus200_andEmptyBody() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/form-template/99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void givenValidFormTemplate_whenCreateFormTemplate_thenStatus201() throws Exception {
        FormTemplate newTemplate = new FormTemplate(
                "Registration Form",
                "User signup form",
                104L,
                "{\"fields\":[\"name\",\"email\",\"password\"]}"
        );
        newTemplate.setIsPublished(false);

        mockMvc.perform(post("/api/" + apiVersion + "/form-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(newTemplate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Registration Form")))
                .andExpect(jsonPath("$.description", is("User signup form")))
                .andExpect(jsonPath("$.isPublished", is(false)))
                .andExpect(jsonPath("$.formJson").isString())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        assertThat(formTemplateRepository.findAll()).hasSize(1);
    }

    @Test
    void givenFormTemplate_whenUpdateFormTemplate_thenStatus200() throws Exception {
        FormTemplate existing = new FormTemplate("Old Form", "Old desc", 105L, "{}");
        existing.setIsPublished(false);
        FormTemplate saved = formTemplateRepository.saveAndFlush(existing);

        FormTemplate updateRequest = new FormTemplate();
        updateRequest.setName("Updated Form");
        updateRequest.setDescription("New description");
        updateRequest.setFormJson("{\"updated\":true}");
        updateRequest.setIsPublished(true);

        mockMvc.perform(put("/api/" + apiVersion + "/form-template/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Form")))
                .andExpect(jsonPath("$.description", is("New description")))
                .andExpect(jsonPath("$.formJson", is("{\"updated\":true}")))
                .andExpect(jsonPath("$.isPublished", is(true)))
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())));

        FormTemplate persisted = formTemplateRepository.findById(saved.getId()).orElse(null);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getName()).isEqualTo("Updated Form");
        assertThat(persisted.getIsPublished()).isTrue();
        assertThat(persisted.getUpdatedAt()).isNotNull();
    }

    @Test
    void givenInvalidId_whenUpdateFormTemplate_thenStatus404() throws Exception {
        FormTemplate updateRequest = new FormTemplate();
        updateRequest.setName("Should Fail");

        mockMvc.perform(put("/api/" + apiVersion + "/form-template/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenFormTemplate_whenDeleteFormTemplate_thenStatus204() throws Exception {
        FormTemplate template = new FormTemplate("To Delete", "Delete me", 106L, "{}");
        FormTemplate saved = formTemplateRepository.saveAndFlush(template);

        mockMvc.perform(delete("/api/" + apiVersion + "/form-template/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(formTemplateRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void givenInvalidId_whenDeleteFormTemplate_thenStatus204() throws Exception {
        mockMvc.perform(delete("/api/" + apiVersion + "/form-template/99999"))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenNullRequestBody_whenCreateFormTemplate_thenStatus400() throws Exception {
        mockMvc.perform(post("/api/" + apiVersion + "/form-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenFormTemplateWithTimestamps_whenCreated_thenTimestampsAreSet() throws Exception {
        FormTemplate template = new FormTemplate("Time Test", "Check timestamps", 109L, "{}");

        mockMvc.perform(post("/api/" + apiVersion + "/form-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(template)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.updatedAt").isString());
    }
}