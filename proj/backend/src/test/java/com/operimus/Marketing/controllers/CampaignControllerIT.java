package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.CampaignStatus;
import com.operimus.Marketing.entities.Dashboard;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.repositories.CampaignRepository;
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

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    classes = {MarketingApplication.class}
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "api.version=v1")
@Import(TestSecurityConfig.class)
public class CampaignControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CampaignRepository campaignRepository;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void resetDb() {
        campaignRepository.deleteAll();
    }

    @Test
    void givenCampaigns_whenGetAllCampaigns_thenStatus200() throws Exception {
        Campaign campaign1 = new Campaign();
        campaign1.setName("Campaign 1");
        campaign1.setDescription("Description 1");
        campaign1.setStatus(CampaignStatus.ACTIVE);
        campaign1.setCreatedAt(new Date());
        campaign1.setDueDate(new Date());
        campaignRepository.saveAndFlush(campaign1);

        Campaign campaign2 = new Campaign();
        campaign2.setName("Campaign 2");
        campaign2.setDescription("Description 2");
        campaign2.setStatus(CampaignStatus.IN_PROGRESS);
        campaign2.setCreatedAt(new Date());
        campaign2.setDueDate(new Date());
        campaignRepository.saveAndFlush(campaign2);

        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Campaign 1")))
                .andExpect(jsonPath("$[1].name", is("Campaign 2")));
    }

    @Test
    void givenCampaign_whenGetCampaignById_thenStatus200() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Campaign X");
        campaign.setDescription("Description X");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Campaign X")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void givenInvalidId_whenGetCampaignById_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenCampaigns_whenGetCampaignsByStatus_thenStatus200() throws Exception {
        Campaign c1 = new Campaign();
        c1.setName("Active Campaign");
        c1.setDescription("Description 1");
        c1.setStatus(CampaignStatus.ACTIVE);
        c1.setCreatedAt(new Date());
        c1.setDueDate(new Date());
        campaignRepository.saveAndFlush(c1);

        Campaign c2 = new Campaign();
        c2.setName("Finished Campaign");
        c2.setDescription("Description 2");
        c2.setStatus(CampaignStatus.FINISHED);
        c2.setCreatedAt(new Date());
        c2.setDueDate(new Date());
        campaignRepository.saveAndFlush(c2);

        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Active Campaign")));
    }

    @Test
    void givenValidCampaign_whenCreateCampaign_thenStatus200() throws Exception {
        Campaign newCampaign = new Campaign();
        newCampaign.setName("New Campaign");
        newCampaign.setDescription("Created via POST");
        newCampaign.setStatus(CampaignStatus.IN_PROGRESS);
        newCampaign.setCreatedAt(new Date());
        newCampaign.setDueDate(new Date());

        mockMvc.perform(post("/api/" + apiVersion + "/campaigns/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(newCampaign)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Campaign")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void givenCampaign_whenUpdateCampaign_thenStatus200() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Original Campaign");
        campaign.setDescription("Before Update");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign saved = campaignRepository.saveAndFlush(campaign);

        Campaign updated = new Campaign();
        updated.setName("Updated Campaign");
        updated.setDescription("After Update");
        updated.setStatus(CampaignStatus.FINISHED);
        updated.setCreatedAt(new Date());
        updated.setDueDate(new Date());

        mockMvc.perform(put("/api/" + apiVersion + "/campaigns/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Campaign")))
                .andExpect(jsonPath("$.status", is("FINISHED")));

        Campaign persisted = campaignRepository.findById(saved.getId()).orElse(null);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getName()).isEqualTo("Updated Campaign");
    }

    @Test
    void givenInvalidId_whenUpdateCampaign_thenStatus404() throws Exception {
        Campaign updated = new Campaign();
        updated.setName("Invalid Update");
        updated.setStatus(CampaignStatus.ACTIVE);

        mockMvc.perform(put("/api/" + apiVersion + "/campaigns/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updated)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenCampaign_whenDeleteCampaign_thenStatus200() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("To Delete");
        campaign.setDescription("Campaign to be deleted");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(delete("/api/" + apiVersion + "/campaigns/" + saved.getId()))
                .andExpect(status().isOk());

        assertThat(campaignRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void givenCampaignWithDashboard_whenGetCampaignDashboard_thenStatus200() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Campaign with Dashboard");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());

        Dashboard dashboard = new Dashboard("Sales Dashboard");
        campaign.setDashboard(dashboard);
        dashboard.setCampaign(campaign);

        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/" + saved.getId() + "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Sales Dashboard")));
    }

    @Test
    void givenCampaignWithoutDashboard_whenGetCampaignDashboard_thenStatus404() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("No Dashboard Campaign");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/" + saved.getId() + "/dashboard"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenInvalidCampaignId_whenGetCampaignDashboard_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/99999/dashboard"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidCampaign_whenCreateCampaignDashboard_thenStatus200() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Campaign for Dashboard Creation");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign savedCampaign = campaignRepository.saveAndFlush(campaign);

        Dashboard newDashboard = new Dashboard("Newly Created Dashboard");

        mockMvc.perform(post("/api/" + apiVersion + "/campaigns/" + savedCampaign.getId() + "/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(newDashboard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Newly Created Dashboard")));

        Campaign updated = campaignRepository.findById(savedCampaign.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getDashboard()).isNotNull();
        assertThat(updated.getDashboard().getTitle()).isEqualTo("Newly Created Dashboard");
    }

    @Test
    void givenInvalidCampaignId_whenCreateCampaignDashboard_thenStatus404() throws Exception {
        Dashboard dashboard = new Dashboard("Should Fail");

        mockMvc.perform(post("/api/" + apiVersion + "/campaigns/99999/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(dashboard)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenNullDashboard_whenCreateCampaignDashboard_thenStatus400() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Test Null Dashboard");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(post("/api/" + apiVersion + "/campaigns/" + saved.getId() + "/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidCampaignAndDashboard_whenUpdateCampaignDashboard_thenStatus200() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Campaign to Update Dashboard");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());

        Dashboard oldDashboard = new Dashboard("Old Dashboard");
        campaign.setDashboard(oldDashboard);
        oldDashboard.setCampaign(campaign);

        Campaign savedCampaign = campaignRepository.saveAndFlush(campaign);

        Dashboard updateData = new Dashboard("Updated Dashboard Title");

        mockMvc.perform(put("/api/" + apiVersion + "/campaigns/" + savedCampaign.getId() + "/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Dashboard Title")));

        Campaign refreshed = campaignRepository.findById(savedCampaign.getId()).orElse(null);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed.getDashboard()).isNotNull();
        assertThat(refreshed.getDashboard().getTitle()).isEqualTo("Updated Dashboard Title");
    }

    @Test
    void givenInvalidCampaignId_whenUpdateCampaignDashboard_thenStatus404() throws Exception {
        Dashboard updateData = new Dashboard("Ignored");

        mockMvc.perform(put("/api/" + apiVersion + "/campaigns/99999/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenNullDashboard_whenUpdateCampaignDashboard_thenStatus400() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Test Null Update");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(put("/api/" + apiVersion + "/campaigns/" + saved.getId() + "/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenCampaignWithWorkflow_whenGetWorkflowByCampaignId_thenStatus200() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("Campaign with Workflow");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());

        Workflow workflow = new Workflow();
        workflow.setName("Sales Workflow");
        workflow.setDescription("Automated email sequence");

        campaign.setWorkflow(workflow);
        workflow.setCampaign(campaign);

        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/" + saved.getId() + "/workflow"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Sales Workflow")))
                .andExpect(jsonPath("$.description", is("Automated email sequence")));
    }

    @Test
    void givenCampaignWithoutWorkflow_whenGetWorkflowByCampaignId_thenStatus200_andNull() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setName("No Workflow Campaign");
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedAt(new Date());
        campaign.setDueDate(new Date());
        Campaign saved = campaignRepository.saveAndFlush(campaign);

        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/" + saved.getId() + "/workflow"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenInvalidCampaignId_whenGetWorkflowByCampaignId_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/campaigns/99999/workflow"))
                .andExpect(status().isNotFound());
    }
}
