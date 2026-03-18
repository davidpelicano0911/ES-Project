package com.operimus.Marketing.services;

import com.operimus.Marketing.dto.EventDTO;
import com.operimus.Marketing.entities.LandingPageEvent;
import com.operimus.Marketing.entities.EventType;
import com.operimus.Marketing.repositories.LandingPageEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.operimus.Marketing.services.LeadService;
import com.operimus.Marketing.entities.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LandingPageEventService {

    @Autowired
    private LandingPageEventRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeadService leadService;

    private static final Logger logger = LoggerFactory.getLogger(LandingPageEventService.class);

    public LandingPageEvent saveEvent(EventDTO dto) {
        try {
            logger.debug("saveEvent called with eventType={} metadata={}", dto != null ? dto.getEventType() : null, dto != null ? dto.getMetadata() : null);
            LandingPageEvent e = new LandingPageEvent();
            // try to map landingPageId from metadata if present (metadata values may be Object)
            Object lpObj = dto.getMetadataValue("landingPageId");
            if (lpObj != null) {
                try {
                    if (lpObj instanceof Number) {
                        e.setLandingPageId(((Number) lpObj).longValue());
                    } else {
                        e.setLandingPageId(Long.valueOf(lpObj.toString()));
                    }
                } catch (NumberFormatException ex) {
                    // ignore parse errors
                }
            }

            // try to map leadId from metadata
            Long resolvedLeadId = null;
            if (dto.getMetadata() != null) {
                // prefer explicit DTO leadId if present
                if (dto.getLeadId() != null) {
                    resolvedLeadId = dto.getLeadId();
                } else {
                    Object leadIdObj = dto.getMetadataValue("leadId");
                    if (leadIdObj == null) {
                        leadIdObj = dto.getMetadataValue("lead_id");
                    }
                    if (leadIdObj != null) {
                        try {
                            if (leadIdObj instanceof Number) {
                                resolvedLeadId = ((Number) leadIdObj).longValue();
                            } else {
                                resolvedLeadId = Long.valueOf(leadIdObj.toString());
                            }
                        } catch (NumberFormatException ex) {
                            // ignore parse errors
                        }
                    }
                }

                // if we don't have a lead id, try to resolve by email (and create lead if missing)
                if (resolvedLeadId == null) {
                    Object emailObj = dto.getMetadataValue("email");
                    if (emailObj == null) {
                        emailObj = dto.getMetadataValue("user"); // legacy key used by frontend in some flows
                    }
                    String email = emailObj != null ? emailObj.toString() : null;
                    if (email != null) {
                        try {
                            Long found = leadService.getLeadIdByEmail(email);
                            if (found != null && found != -1L) {
                                resolvedLeadId = found;
                            } else {
                                // create a minimal lead record with the email
                                try {
                                    Lead newLead = new Lead();
                                    newLead.setEmail(email);
                                    Lead saved = leadService.createLead(newLead);
                                    if (saved != null) resolvedLeadId = saved.getId();
                                } catch (Exception createEx) {
                                    // if create failed, try to read id again (race)
                                    Long retry = leadService.getLeadIdByEmail(email);
                                    if (retry != null && retry != -1L) resolvedLeadId = retry;
                                }
                            }
                        } catch (Exception ex) {
                            // ignore resolution errors
                        }
                    }
                }
            }

            if (resolvedLeadId != null) {
                e.setLeadId(resolvedLeadId);
            }

            e.setEventType(dto.getEventType() != null ? dto.getEventType() : EventType.LANDING_PAGE_OPENED);

            // serialize metadata map to JSON for storage
            try {
                e.setMetadataJson(objectMapper.writeValueAsString(dto.getMetadata()));
            } catch (Exception ex) {
                e.setMetadataJson(null);
            }

            LandingPageEvent saved = repository.save(e);
            logger.debug("Saved LandingPageEvent id={} leadId={} eventType={}", saved != null ? saved.getId() : null, saved != null ? saved.getLeadId() : null, saved != null ? saved.getEventType() : null);
            return saved;
        } catch (Exception ex) {
            // don't throw - swallow and return null on failure
            logger.error("Error saving LandingPageEvent", ex);
            return null;
        }
    }

    public java.util.List<LandingPageEvent> getByLeadId(Long leadId) {
        try {
            logger.debug("getByLeadId called with leadId={}", leadId);
            if (leadId == null) {
                logger.debug("leadId is null, returning empty list");
                return new java.util.ArrayList<>();
            }
            java.util.List<LandingPageEvent> results = repository.findByLeadId(leadId);
            logger.debug("repository.findByLeadId returned {} rows for leadId={}", results != null ? results.size() : 0, leadId);
            return results != null ? results : new java.util.ArrayList<>();
        } catch (Exception ex) {
            logger.error("Error while fetching landing page events for leadId={}", leadId, ex);
            return new java.util.ArrayList<>();
        }
    }
}
