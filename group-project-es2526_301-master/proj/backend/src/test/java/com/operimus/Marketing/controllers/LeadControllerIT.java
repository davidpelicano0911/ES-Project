package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.repositories.LeadRepository;
import com.operimus.Marketing.security.TestSecurityConfig;
import com.operimus.Marketing.utils.JsonUtils;
import com.operimus.Marketing.services.HubSpotLeadService;
import com.operimus.Marketing.services.MarketingIndexerService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.operimus.Marketing.services.MarketingIndexerService;

@SpringBootTest(classes = {MarketingApplication.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "api.version=v1")
@Import(TestSecurityConfig.class)
class LeadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadRepository leadRepository;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    @Value("${api.version}")
    private String apiVersion;

    // ---------------- CREATE ----------------
    @Test
    void givenValidLead_whenCreateLead_thenStatus201AndReturnLead() throws Exception {
        Lead lead = new Lead();
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setEmail("john.doe@example.com");

        mockMvc.perform(post("/api/" + apiVersion + "/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(lead)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        List<Lead> allLeads = leadRepository.findAll();
        assertThat(allLeads).isNotEmpty();
        assertThat(allLeads.get(allLeads.size() - 1).getFirstName()).isEqualTo("John");
    }

    // ---------------- GET ALL ----------------
    @Test
    void whenGetAllLeads_thenStatus200AndReturnList() throws Exception {
        // Ensure at least one lead exists
        Lead lead = new Lead();
        lead.setFirstName("Alice");
        lead.setLastName("Smith");
        lead.setEmail("alice@example.com");
        leadRepository.saveAndFlush(lead);

        mockMvc.perform(get("/api/" + apiVersion + "/leads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ---------------- GET BY ID ----------------
    @Test
    void givenExistingLead_whenGetLeadById_thenStatus200() throws Exception {
        Lead lead = new Lead();
        lead.setFirstName("Bob");
        lead.setLastName("Brown");
        lead.setEmail("bob@example.com");
        Lead saved = leadRepository.saveAndFlush(lead);

        mockMvc.perform(get("/api/" + apiVersion + "/leads/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Bob")))
                .andExpect(jsonPath("$.lastName", is("Brown")));
    }

    @Test
    void givenNonExistingLead_whenGetLeadById_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/leads/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------------- UPDATE ----------------
    @Test
    void givenLead_whenUpdateLead_thenStatus200AndPersisted() throws Exception {
        Lead lead = new Lead();
        lead.setFirstName("Charlie");
        lead.setLastName("Davis");
        lead.setEmail("charlie@example.com");
        Lead saved = leadRepository.saveAndFlush(lead);

        Lead updated = new Lead();
        updated.setFirstName("CharlieUpdated");
        updated.setLastName("DavisUpdated");
        updated.setEmail("charlie.updated@example.com");

        mockMvc.perform(put("/api/" + apiVersion + "/leads/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("CharlieUpdated")))
                .andExpect(jsonPath("$.lastName", is("DavisUpdated")))
                .andExpect(jsonPath("$.email", is("charlie.updated@example.com")));

        Lead persisted = leadRepository.findById(saved.getId()).orElse(null);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getFirstName()).isEqualTo("CharlieUpdated");
        assertThat(persisted.getLastName()).isEqualTo("DavisUpdated");
        assertThat(persisted.getEmail()).isEqualTo("charlie.updated@example.com");
    }

    @Test
    void givenNonExistingLead_whenUpdateLead_thenStatus404() throws Exception {
        Lead updated = new Lead();
        updated.setFirstName("NoOne");

        mockMvc.perform(put("/api/" + apiVersion + "/leads/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(updated)))
                .andExpect(status().isNotFound());
    }

    // ---------------- DELETE ----------------
    @Test
    void givenExistingLead_whenDeleteLead_thenStatus204() throws Exception {
        Lead lead = new Lead();
        lead.setFirstName("Eve");
        lead.setLastName("White");
        lead.setEmail("eve@example.com");
        Lead saved = leadRepository.saveAndFlush(lead);

        mockMvc.perform(delete("/api/" + apiVersion + "/leads/" + saved.getId()))
                .andExpect(status().isNoContent());

        Lead deleted = leadRepository.findById(saved.getId()).orElse(null);
        assertThat(deleted).isNull();
    }

    @Test
    void givenNonExistingLead_whenDeleteLead_thenStatus404() throws Exception {
        mockMvc.perform(delete("/api/" + apiVersion + "/leads/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenDuplicateEmail_whenCreateLead_thenStatus400() throws Exception {
        // Create first lead
        Lead lead1 = new Lead();
        lead1.setEmail("duplicate@example.com");
        lead1.setFirstName("First");
        leadRepository.saveAndFlush(lead1);

        // Try to create second with same email
        Lead lead2 = new Lead();
        lead2.setEmail("duplicate@example.com");
        lead2.setFirstName("Second");

        mockMvc.perform(post("/api/" + apiVersion + "/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(lead2)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void givenDuplicatePhoneNumber_whenCreateLead_thenStatus400() throws Exception {
        // Create first lead
        Lead lead1 = new Lead();
        lead1.setPhoneNumber("+1234567890");
        lead1.setEmail("test@example.com");
        lead1.setFirstName("First");
        leadRepository.saveAndFlush(lead1);

        // Try to create second with same phone number
        Lead lead2 = new Lead();
        lead2.setPhoneNumber("+1234567890");
        lead2.setEmail("test2@example.com");
        lead2.setFirstName("Second");
        mockMvc.perform(post("/api/" + apiVersion + "/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(lead2)))
            .andExpect(status().isBadRequest());

    }
}
