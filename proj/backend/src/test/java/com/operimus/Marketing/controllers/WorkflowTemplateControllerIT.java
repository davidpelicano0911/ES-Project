package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.entities.WorkflowTemplate;
import com.operimus.Marketing.repositories.WorkflowTemplateRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.operimus.Marketing.services.MarketingIndexerService;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {MarketingApplication.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "api.version=v1")
@Import(TestSecurityConfig.class)
class WorkflowTemplateControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkflowTemplateRepository templateRepository;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void resetDb() {
        templateRepository.deleteAll();
    }

    @Test
    void givenTemplates_whenGetAll_thenStatus200_andReturnsList() throws Exception {
        WorkflowTemplate template1 = new WorkflowTemplate();
        template1.setName("Template 1");
        template1.setDescription("Description 1");
        templateRepository.saveAndFlush(template1);

        WorkflowTemplate template2 = new WorkflowTemplate();
        template2.setName("Template 2");
        template2.setDescription("Description 2");
        templateRepository.saveAndFlush(template2);

        mockMvc.perform(get("/api/" + apiVersion + "/workflow-templates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Template 1")))
                .andExpect(jsonPath("$[1].name", is("Template 2")));
    }

    @Test
    void givenTemplate_whenGetById_thenStatus200_andCorrectTemplate() throws Exception {
        WorkflowTemplate template1 = new WorkflowTemplate();
        template1.setName("Template 1");
        template1.setDescription("Description 1");
        templateRepository.saveAndFlush(template1);

        mockMvc.perform(get("/api/" + apiVersion + "/workflow-templates/" + template1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Template 1")))
                .andExpect(jsonPath("$.description", is("Description 1")));
    }

    @Test
    void givenInvalidId_whenGetById_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/workflow-templates/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidTemplate_whenCreate_thenStatus200_andPersisted() throws Exception {
        WorkflowTemplate template1 = new WorkflowTemplate();
        template1.setName("Template 1");
        template1.setDescription("Description 1");

        mockMvc.perform(post("/api/" + apiVersion + "/workflow-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(template1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Template 1")))
                .andExpect(jsonPath("$.description", is("Description 1")));

        assertThat(templateRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(t -> {
                    assertThat(t.getName()).isEqualTo("Template 1");
                    assertThat(t.getDescription()).isEqualTo("Description 1");
                });
    }

    @Test
    void givenTemplate_whenUpdate_thenStatus200_andChangesPersisted() throws Exception {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName("Old Name");
        template.setDescription("Old description");
        WorkflowTemplate saved = templateRepository.saveAndFlush(template);

        WorkflowTemplate update = new WorkflowTemplate();
        update.setName("New Name");
        update.setDescription("New description");

        mockMvc.perform(put("/api/" + apiVersion + "/workflow-templates/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.description", is("New description")));
    }

    @Test
    void givenInvalidId_whenUpdate_thenStatus404() throws Exception {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName("Old Name");
        template.setDescription("Old description");

        mockMvc.perform(put("/api/" + apiVersion + "/workflow-templates/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(template)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenTemplate_whenDelete_thenStatus204_andRemoved() throws Exception {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName("To be deleted");
        template.setDescription("To be deleted description");
        WorkflowTemplate saved = templateRepository.saveAndFlush(template);

        mockMvc.perform(delete("/api/" + apiVersion + "/workflow-templates/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(templateRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void givenInvalidId_whenDelete_thenStatus404() throws Exception {
        mockMvc.perform(delete("/api/" + apiVersion + "/workflow-templates/99999"))
                .andExpect(status().isNotFound());
    }
}