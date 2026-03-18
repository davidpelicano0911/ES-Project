package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.SegmentsRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.operimus.Marketing.services.MarketingIndexerService;

@SpringBootTest(classes = {MarketingApplication.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "api.version=v1")
@Import(TestSecurityConfig.class)
public class SegmentsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SegmentsRepository segmentsRepository;

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
        // Delete campaigns first to avoid foreign key constraint violations
        campaignRepository.deleteAll();
        segmentsRepository.deleteAll();
    }

    @Test
    void givenSegments_whenGetAllSegments_thenStatus200() throws Exception {
        Segments segment1 = new Segments();
        segment1.setName("Segment A");
        segmentsRepository.saveAndFlush(segment1);

        Segments segment2 = new Segments();
        segment2.setName("Segment B");
        segmentsRepository.saveAndFlush(segment2);

        mockMvc.perform(get("/api/" + apiVersion + "/segments/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Segment A")))
                .andExpect(jsonPath("$[1].name", is("Segment B")));
    }

    @Test
    void givenNoSegments_whenGetAllSegments_thenStatus200_andEmptyList() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/segments/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenValidSegment_whenCreateSegment_thenStatus200_andSegmentReturned() throws Exception {
        Segments newSegment = new Segments();
        newSegment.setName("New Segment");

        mockMvc.perform(post("/api/" + apiVersion + "/segments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(newSegment)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("New Segment")))
                .andExpect(jsonPath("$.id").isNotEmpty());

        // Verify persistence
        assertThat(segmentsRepository.findAll()).hasSize(1);
        Segments saved = segmentsRepository.findAll().get(0);
        assertThat(saved.getName()).isEqualTo("New Segment");
    }

    @Test
    void givenSegmentWithNullName_whenCreateSegment_thenStatus400() throws Exception {
        Segments invalidSegment = new Segments();
        invalidSegment.setName(null);

        mockMvc.perform(post("/api/" + apiVersion + "/segments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(invalidSegment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenSegmentWithEmptyName_whenCreateSegment_thenStatus400() throws Exception {
        Segments invalidSegment = new Segments();
        invalidSegment.setName("");

        mockMvc.perform(post("/api/" + apiVersion + "/segments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(invalidSegment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNullBody_whenCreateSegment_thenStatus400() throws Exception {
        mockMvc.perform(post("/api/" + apiVersion + "/segments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidJson_whenCreateSegment_thenStatus400() throws Exception {
        mockMvc.perform(post("/api/" + apiVersion + "/segments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
}