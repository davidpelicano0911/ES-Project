package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Lead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HubSpotLeadServiceTest {
    private HubSpotLeadService hubSpotLeadService;

    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;

    private Lead lead;

    @BeforeEach
    void setup() {
        hubSpotLeadService = new HubSpotLeadService();

        restTemplate = (RestTemplate) ReflectionTestUtils.getField(
                hubSpotLeadService, "restTemplate"
        );

        mockServer = MockRestServiceServer.createServer(restTemplate);

        ReflectionTestUtils.setField(hubSpotLeadService, "accessToken", "TEST_TOKEN");

        lead = new Lead();
        lead.setCrmId("123");
        lead.setEmail("john@example.com");
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setPhoneNumber("+1111111");
        lead.setCountry("PT");
        lead.setScore(5);
        lead.setStatus("NEW");
    }

    @Test
    void whenCreateLeadInHubSpot_thenReturnCrmId() {
        String expectedBody = """
        {
          "id": "HS123"
        }
        """;

        mockServer.expect(once(), requestTo("https://api.hubapi.com/crm/v3/objects/contacts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer TEST_TOKEN"))
                .andRespond(withSuccess(expectedBody, MediaType.APPLICATION_JSON));

        String id = hubSpotLeadService.createLeadInHubSpot(lead);

        assertThat(id).isEqualTo("HS123");

        mockServer.verify();
    }

    @Test
    void whenUpdateLeadInHubSpot_thenPatchRequestIsSent() {
        mockServer.expect(once(),
                requestTo("https://api.hubapi.com/crm/v3/objects/contacts/123"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        hubSpotLeadService.updateLeadInHubSpot(lead);

        mockServer.verify();
    }

    @Test
    void whenDeleteLeadInHubSpot_thenDeleteRequestIsSent() {
        mockServer.expect(once(),
                requestTo("https://api.hubapi.com/crm/v3/objects/contacts/123"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        hubSpotLeadService.deleteLeadInHubSpot(lead);

        mockServer.verify();
    }

    @Test
    void whenFetchHubSpotContacts_thenReturnParsedList() {
        String jsonResponse = """
        {
            "results": [
                { "id": "1", "properties": { "email": "one@example.com" }},
                { "id": "2", "properties": { "email": "two@example.com" }}
            ]
        }
        """;

        mockServer.expect(once(),
                requestTo(org.hamcrest.Matchers.containsString("/crm/v3/objects/contacts")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<Map<String, Object>> results = hubSpotLeadService.fetchHubSpotContacts();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("id")).isEqualTo("1");
        assertThat(results.get(1).get("id")).isEqualTo("2");

        mockServer.verify();
    }
}
