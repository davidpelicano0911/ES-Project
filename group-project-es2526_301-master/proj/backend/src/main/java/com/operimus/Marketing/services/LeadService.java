package com.operimus.Marketing.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.operimus.Marketing.dto.LeadSyncStatusDTO;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.repositories.LeadRepository;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;


@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private HubSpotLeadService hubSpotLeadService;
    
    public Lead createLead(Lead lead) {
        if (leadRepository.existsByEmail(lead.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A lead with this email already exists");
        }
        if (lead.getPhoneNumber() != null && leadRepository.existsByPhoneNumber(lead.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A lead with this phone number already exists");
        }
        lead.addSegment(1L);
        Lead saved = leadRepository.save(lead);
        String crmId = hubSpotLeadService.createLeadInHubSpot(saved);
        saved.setCrmId(crmId);

        Date now = new Date();
        saved.setLastUpdatedAt(now);
        saved.setLastSyncedAt(now);

        return leadRepository.save(saved);
    }

    public Lead getLead(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id));
    }

    public Lead updateLead(Long id, Lead lead) {
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id));

        // Check email uniqueness safely (handle nulls)
        if (lead.getEmail() != null && !Objects.equals(existingLead.getEmail(), lead.getEmail()) &&
            leadRepository.existsByEmail(lead.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A lead with this email already exists");
        }
        // Check phone uniqueness safely (handle null existing phone)
        if (lead.getPhoneNumber() != null &&
            !Objects.equals(existingLead.getPhoneNumber(), lead.getPhoneNumber()) &&
            leadRepository.existsByPhoneNumber(lead.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A lead with this phone number already exists");
        }

        if (lead.getFirstName() != null) existingLead.setFirstName(lead.getFirstName());
        if (lead.getLastName() != null) existingLead.setLastName(lead.getLastName());
        if (lead.getEmail() != null) existingLead.setEmail(lead.getEmail());
        if (lead.getPhoneNumber() != null) existingLead.setPhoneNumber(lead.getPhoneNumber());
        if (lead.getCountry() != null) existingLead.setCountry(lead.getCountry());

        Lead savedLead = leadRepository.save(existingLead);

        if (savedLead.getCrmId() != null) {
            hubSpotLeadService.updateLeadInHubSpot(savedLead);
        }

        Date now = new Date();
        savedLead.setLastUpdatedAt(now);
        savedLead.setLastSyncedAt(now);

        return leadRepository.save(savedLead);
    }


    public boolean deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id);
        }

        Lead lead = leadRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id));

        if (lead.getCrmId() != null) {
            hubSpotLeadService.deleteLeadInHubSpot(lead);
        }

        leadRepository.deleteById(id);
        return true;
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    /**
     * Return lead id for a given email or -1 if not found.
     */
    public Long getLeadIdByEmail(String email) {
        if (email == null) return -1L;
        return leadRepository.findByEmail(email).map(Lead::getId).orElse(-1L);
    }

    /**
     * Return lead id for a given phone number or -1 if not found.
     */
    public Long getLeadIdByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return -1L;
        return leadRepository.findByPhoneNumber(phoneNumber).map(Lead::getId).orElse(-1L);
    }

    public Map<String, Object> syncAllFromHubSpot() {
        List<Map<String, Object>> contacts = hubSpotLeadService.fetchHubSpotContacts();

        int updatedCount = 0;
        int createdCount = 0;

        for (Map<String, Object> contact : contacts) {
            String crmId = (String) contact.get("id");
            Map<String, Object> props = (Map<String, Object>) contact.get("properties");

            String email = (String) props.get("email");
            String firstname = (String) props.get("firstname");
            String lastname = (String) props.get("lastname");
            String phone = (String) props.get("phone");
            String country = (String) props.get("country");
            String scoreStr = (String) props.get("score");
            Integer score = scoreStr != null ? Integer.valueOf(scoreStr) : null;

            String lastModified = (String) props.get("lastmodifieddate");
            Date hubspotUpdatedAt = lastModified != null
                    ? Date.from(Instant.parse(lastModified))
                    : new Date(0);

            Lead existing = leadRepository.findByCrmId(crmId).orElse(null);

            if (existing == null && email != null) {
                existing = leadRepository.findByEmail(email).orElse(null);

                if (existing != null) {
                    existing.setCrmId(crmId);
                }
            }

            if (existing == null) {
                Lead newLead = new Lead();
                newLead.setCrmId(crmId);
                newLead.setEmail(email);
                newLead.setFirstName(firstname);
                newLead.setLastName(lastname);
                newLead.setPhoneNumber(phone);
                newLead.setCountry(country);
                if (score != null) newLead.setScore(score);

                newLead.setLastUpdatedAt(hubspotUpdatedAt);
                newLead.setLastSyncedAt(new Date());

                leadRepository.save(newLead);
                createdCount++;
                continue;
            }

            if (existing.getLastUpdatedAt() == null ||
                    hubspotUpdatedAt.after(existing.getLastUpdatedAt())) {

                existing.setEmail(email);
                existing.setFirstName(firstname);
                existing.setLastName(lastname);
                existing.setPhoneNumber(phone);
                existing.setCountry(country);
                
                if (score != null) {
                    existing.setScore(score);
                }

                existing.setLastUpdatedAt(hubspotUpdatedAt);
                existing.setLastSyncedAt(new Date());

                leadRepository.save(existing);
                updatedCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created", createdCount);
        result.put("updated", updatedCount);
        result.put("lastSyncedAt", new Date());

        return result;
    }

    public LeadSyncStatusDTO getSyncStatus() {
        List<Map<String, Object>> contacts = hubSpotLeadService.fetchHubSpotContacts();

        int pendingUpdates = 0;
        int pendingCreates = 0;
        Date latestSync = leadRepository.findLatestSyncDate();

        for (Map<String, Object> contact : contacts) {
            String crmId = (String) contact.get("id");
            Map<String, Object> props = (Map<String, Object>) contact.get("properties");

            String email = (String) props.get("email");

            String lastModified = (String) props.get("lastmodifieddate");
            Date hubspotDate = lastModified != null
                    ? Date.from(Instant.parse(lastModified))
                    : new Date(0);

            Lead existing = leadRepository.findByCrmId(crmId)
                    .orElseGet(() -> email == null ? null : leadRepository.findByEmail(email).orElse(null));

            if (existing == null) {
                pendingCreates++;
                continue;
            }

            if (existing.getLastSyncedAt() == null ||
                    hubspotDate.after(existing.getLastSyncedAt())) {

                pendingUpdates++;
            }
        }

        LeadSyncStatusDTO status = new LeadSyncStatusDTO();
        status.pendingCreates = pendingCreates;
        status.pendingUpdates = pendingUpdates;
        status.updatesAvailable = (pendingCreates + pendingUpdates) > 0;
        status.lastSyncedAt = latestSync;

        return status;
    }

}
