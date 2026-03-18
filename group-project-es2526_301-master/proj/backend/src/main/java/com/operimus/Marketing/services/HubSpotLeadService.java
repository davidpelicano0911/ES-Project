package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Lead;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HubSpotLeadService {

    @Value("${hubspot.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate;

    private static final String HUBSPOT_CONTACTS_URL =
            "https://api.hubapi.com/crm/v3/objects/contacts";

    public HubSpotLeadService() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    private Map<String, Object> buildProperties(Lead lead) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", lead.getEmail());
        properties.put("firstname", lead.getFirstName());
        properties.put("lastname", lead.getLastName());
        properties.put("country", lead.getCountry());
        properties.put("phone", lead.getPhoneNumber());
        properties.put("hs_lead_status", lead.getStatus()); 
        properties.put("score", lead.getScore() != null ? lead.getScore().toString() : "0");
        return properties;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    public String createLeadInHubSpot(Lead lead) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("properties", buildProperties(lead));

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(payload, buildHeaders());

        ResponseEntity<Map> response =
                restTemplate.postForEntity(HUBSPOT_CONTACTS_URL, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            System.out.println("Lead created in HubSpot with id: " + response.getBody().get("id"));
            return (String) response.getBody().get("id"); // HubSpot objectId
        }

        throw new RuntimeException("Failed to create lead in HubSpot: " + response.getStatusCode());
    }

    public void updateLeadInHubSpot(Lead lead) {

        if (lead.getCrmId() == null) {
            throw new IllegalStateException("Cannot update lead in HubSpot: crmId is null.");
        }

        String url = HUBSPOT_CONTACTS_URL + "/" + lead.getCrmId();

        Map<String, Object> payload = new HashMap<>();
        payload.put("properties", buildProperties(lead));

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(payload, buildHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update lead in HubSpot: " + response.getStatusCode());
        }

        System.out.println("Lead with crmId " + lead.getCrmId() + " updated in HubSpot.");
    }

    public void deleteLeadInHubSpot(Lead lead) {
        if (lead.getCrmId() == null) {
            return;
        }

        String url = HUBSPOT_CONTACTS_URL + "/" + lead.getCrmId();

        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful() 
                && response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Failed to delete lead from HubSpot: " + response.getStatusCode());
        }

        System.out.println("Lead with crmId " + lead.getCrmId() + " deleted from HubSpot.");
    }

    public List<Map<String, Object>> fetchHubSpotContacts() {
        String url = HUBSPOT_CONTACTS_URL
            + "?limit=100&archived=false"
            + "&properties=email,firstname,lastname,phone,country,score,hs_lead_status,lastmodifieddate";

        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch HubSpot contacts: " + response.getStatusCode());
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");

        return results;
    }


}
