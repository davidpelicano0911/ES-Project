package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.FormSubmission;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.entities.FormTemplate;
import com.operimus.Marketing.repositories.FormSubmissionRepository;
import com.operimus.Marketing.repositories.FormTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import com.operimus.Marketing.dto.EventDTO;
import com.operimus.Marketing.entities.EventType;
import com.operimus.Marketing.services.WorkflowEngine;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

@Service
public class FormSubmissionService {

    @Autowired
    private FormSubmissionRepository submissionRepository;

    @Autowired
    private FormTemplateRepository templateRepository;

    @Autowired
    private LeadService leadService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkflowEngine workflowEngine;

    @Transactional
    private FormSubmission processSubmission(FormSubmission submission, Long formId) {
        if (formId != null) {
            submission.setFormId(formId);
        }
        // Delegate lead resolution (find/create/update) to helper
        resolveLeadFromResponses(submission);

        FormSubmission saved = submissionRepository.save(submission);

        // Publish event so workflow engine can pick it up (only if we have a lead)
        if (saved.getLeadId() != null) {
            try {
                EventDTO event = new EventDTO();
                event.setEventType(EventType.FORM_SUBMITTED);
                event.setLeadId(saved.getLeadId());
                Map<String, Object> metadata = new HashMap<>();
                if (formId != null) metadata.put("formId", formId);
                if (saved.getCampaignId() != null) metadata.put("campaignId", saved.getCampaignId());
                event.setMetadata(metadata);
                workflowEngine.handleEvent(event);
            } catch (Exception ex) {
                // Don't fail the submission if workflow handling fails; log if desired
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<FormSubmission> getAll() {
        return submissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FormSubmission> getByForm(Long formId) {
        return submissionRepository.findByFormId(formId);
    }

    @Transactional(readOnly = true)
    public List<FormSubmission> getByCampaign(Long campaignId) {
        return submissionRepository.findByCampaignId(campaignId);
    }

    @Transactional(readOnly = true)
    public List<FormSubmission> getByLead(Long leadId) {
        return submissionRepository.findByLeadId(leadId);
    }

    @Transactional(readOnly = true)
    public List<FormSubmission> getSubmissions(Long formId, Long campaignId, Long leadId) {
        if (formId != null) return getByForm(formId);
        if (campaignId != null) return getByCampaign(campaignId);
        if (leadId != null) return getByLead(leadId);
        return getAll();
    }

    public FormSubmission createSubmission(Map<String, Object> body) {
        Long formId = body.get("formId") != null ? Long.valueOf(String.valueOf(body.get("formId"))) : null;
        Long campaignId = body.get("campaignId") != null ? Long.valueOf(String.valueOf(body.get("campaignId"))) : null;
        Long leadId = body.get("leadId") != null ? Long.valueOf(String.valueOf(body.get("leadId"))) : null;

        Object responsesObj = body.get("responses");
        String json;
        try {
            json = objectMapper.writeValueAsString(responsesObj != null ? responsesObj : new HashMap<>());
        } catch (Exception e) {
            // If serialization fails, fallback to empty JSON
            json = "{}";
        }

        FormSubmission s = new FormSubmission();
        s.setLeadId(leadId);
        s.setCampaignId(campaignId);
        s.setResponsesJson(json);

        return processSubmission(s, formId);
    }

    private void resolveLeadFromResponses(FormSubmission submission) {
        if (submission.getLeadId() != null) return;

        String responses = submission.getResponsesJson();
        if (responses == null || responses.isEmpty()) return;

        try {
            Map<String, Object> map = objectMapper.readValue(responses, new TypeReference<Map<String, Object>>(){});

            String email = null;
            String phone = null;
            String firstName = null;
            String lastName = null;
            String country = null;

            for (Map.Entry<String, Object> e : map.entrySet()) {
                String key = e.getKey() == null ? "" : e.getKey().toLowerCase(Locale.ROOT);
                Object val = e.getValue();
                if (val == null) continue;
                String s = String.valueOf(val).trim();
                if (s.isEmpty()) continue;

                if (email == null && key.contains("email")) email = s;
                if (phone == null && (key.contains("phone") || key.contains("phone_number") || key.contains("phonenumber") || key.contains("mobile"))) phone = s;
                if (country == null && key.contains("country")) country = s;
                if (firstName == null && (key.contains("first") && key.contains("name") || key.equals("firstname") || key.equals("first_name"))) firstName = s;
                if (lastName == null && (key.contains("last") && key.contains("name") || key.equals("lastname") || key.equals("last_name"))) lastName = s;
            }

            Long foundId = -1L;
            if (email != null) foundId = leadService.getLeadIdByEmail(email);
            if (foundId == -1L && phone != null) foundId = leadService.getLeadIdByPhoneNumber(phone);

            if (foundId != -1L) {
                // update existing lead with missing fields
                try {
                    Lead existing = leadService.getLead(foundId);
                    Lead toUpdate = new Lead();
                    boolean needsUpdate = false;

                    if ((existing.getPhoneNumber() == null || existing.getPhoneNumber().isEmpty()) && phone != null) {
                        toUpdate.setPhoneNumber(phone);
                        needsUpdate = true;
                    }
                    if ((existing.getFirstName() == null || existing.getFirstName().isEmpty()) && firstName != null) {
                        toUpdate.setFirstName(firstName);
                        needsUpdate = true;
                    }
                    if ((existing.getLastName() == null || existing.getLastName().isEmpty()) && lastName != null) {
                        toUpdate.setLastName(lastName);
                        needsUpdate = true;
                    }
                    if ((existing.getCountry() == null || existing.getCountry().isEmpty()) && country != null) {
                        toUpdate.setCountry(country);
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        try {
                            leadService.updateLead(foundId, toUpdate);
                        } catch (Exception ex) {
                            // ignore update failures
                        }
                    }
                } catch (Exception ex) {
                    // ignore
                }

                submission.setLeadId(foundId);
            } else {
                if (email != null || phone != null) {
                    Lead newLead = new Lead();
                    if (firstName != null) newLead.setFirstName(firstName);
                    if (lastName != null) newLead.setLastName(lastName);
                    if (email != null) newLead.setEmail(email);
                    if (phone != null) newLead.setPhoneNumber(phone);
                    if (country != null) newLead.setCountry(country);

                    try {
                        Lead saved = leadService.createLead(newLead);
                        submission.setLeadId(saved.getId());
                    } catch (Exception ex) {
                        Long retryId = -1L;
                        if (email != null) retryId = leadService.getLeadIdByEmail(email);
                        if (retryId == -1L && phone != null) retryId = leadService.getLeadIdByPhoneNumber(phone);
                        if (retryId != -1L) submission.setLeadId(retryId);
                    }
                }
            }
        } catch (Exception ex) {
            // ignore parsing errors
        }
    }


    public List<Map<String, Object>> getFormPerformanceStats() {
        // CORREÇÃO AQUI: Usa o nome 'countSubmissionsByForm' que definiste no Repository
        List<Object[]> rawStats = submissionRepository.countSubmissionsByForm();
        
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Object[] row : rawStats) {
            Long formId = (Long) row[0];
            Long count = (Long) row[1];

            if (formId == null) continue;

            String formName = templateRepository.findById(formId)
                    .map(FormTemplate::getName) 
                    .orElse("Form ID " + formId);

            Map<String, Object> statItem = new HashMap<>();
            statItem.put("name", formName);
            statItem.put("subs", count);
            statItem.put("views", 0); 

            resultList.add(statItem);
        }

        return resultList;
    }
}
