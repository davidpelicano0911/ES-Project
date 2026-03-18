package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.LandingPage;
import com.operimus.Marketing.repositories.LandingPageRepository;
import com.operimus.Marketing.security.TestSecurityConfig;
import com.operimus.Marketing.services.HubSpotLeadService;
import com.operimus.Marketing.services.MarketingIndexerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.operimus.Marketing.MarketingApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.operimus.Marketing.services.MarketingIndexerService;


@SpringBootTest(
    classes = {MarketingApplication.class}
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "api.version=v1")
@Import(TestSecurityConfig.class)
public class LandingPageControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LandingPageRepository repository;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    @Autowired
    private ObjectMapper objectMapper;

    private LandingPage samplePage;

    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        samplePage = new LandingPage();
        samplePage.setName("Test Landing Page");
        samplePage.setDescription("A simple test page");
        samplePage.setBody("<h1>Hello World</h1>");
        samplePage.setDesign("{\"version\":1}");
        repository.save(samplePage);
    }

    @Test
    void whenGetAll_thenStatus200AndReturnPages() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/landing-pages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Landing Page"));
    }

    @Test
    void whenGetById_thenReturnLandingPage() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/landing-pages/" + samplePage.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("A simple test page"));
    }

    @Test
    void whenCreate_thenReturnCreated() throws Exception {
        LandingPage newPage = new LandingPage();
        newPage.setName("Created Page");
        newPage.setDescription("Created from test");
        newPage.setBody("<p>Example</p>");
        newPage.setDesign("{}");

        mockMvc.perform(post("/api/" + apiVersion + "/landing-pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPage)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Created Page"));
    }

    @Test
    void whenUpdate_thenReturnUpdated() throws Exception {
        samplePage.setDescription("Updated description");

        mockMvc.perform(put("/api/" + apiVersion + "/landing-pages/" + samplePage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void whenDelete_thenReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/" + apiVersion + "/landing-pages/" + samplePage.getId()))
                .andExpect(status().isNoContent());
    }
}
