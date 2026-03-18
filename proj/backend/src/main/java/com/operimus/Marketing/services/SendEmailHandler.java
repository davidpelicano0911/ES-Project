    package com.operimus.Marketing.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.operimus.Marketing.entities.EmailTemplate;
import com.operimus.Marketing.entities.SendEmailAction;
import com.operimus.Marketing.entities.WorkflowInstance;

@Service
public class SendEmailHandler implements NodeHandler<SendEmailAction> {

    private static final Logger logger = LoggerFactory.getLogger(SendEmailHandler.class);

    @Autowired
    private MailJetService mailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private EmailLogService emailLogService;

    @Autowired
    private LeadService leadService;

    @Override
    public Class<SendEmailAction> getHandledType() {
        return SendEmailAction.class;
    }

    @Override
    public WorkflowInstance execute(SendEmailAction node, WorkflowInstance instance) {
        logger.info("Executing SendEmailAction node ID: {}", node.getId());
        
        EmailTemplate template = emailTemplateService.getTemplateById(node.getEmailTemplateId())
                .orElseThrow(() -> new IllegalStateException("Email template not found with ID: " + node.getEmailTemplateId()));
        
        // Send email and get the MailJet message ID
        Long mailjetMessageId = mailService.sendEmail(instance.getLead(), node.getSendFrom(), template.getSubject(), template.getBody());
        
        logger.debug("SendEmailHandler - After sendEmail call. MessageID: {}, Lead: {}", mailjetMessageId, instance.getLead().getId());
        
        // Log the email - always log, even without campaign context
        if (mailjetMessageId != null && mailjetMessageId > 0) {
            Long campaignId = instance.getWorkflow() != null && instance.getWorkflow().getCampaign() != null
                ? instance.getWorkflow().getCampaign().getId()
                : null;
            
            // Look up the lead by email to get the lead ID if it exists in the system
            Long leadId = instance.getLead().getId();
            String email = instance.getLead().getEmail();
            
            // If lead ID is not set, try to find it by email
            if (leadId == null || leadId <= 0) {
                try {
                    Long foundLeadId = leadService.getLeadIdByEmail(email);
                    if (foundLeadId != null && foundLeadId > 0) {
                        leadId = foundLeadId;
                        logger.info("Found existing lead by email: {} -> {}", email, leadId);
                    }
                } catch (Exception e) {
                    logger.debug("Could not look up lead by email: {}", email);
                }
            }
            
            // Log the email with all available data
            emailLogService.logEmail(
                campaignId,
                leadId,  // Will be null if lead doesn't exist in system
                email,
                mailjetMessageId,
                template.getSubject()
            );
            logger.info("Email logged successfully: Campaign={}, Lead={}, Email={}, MessageID={}", 
                campaignId, leadId, email, mailjetMessageId);
        } else {
            logger.error("SendEmailHandler - Email send failed or returned invalid ID. MessageID: {}", mailjetMessageId);
        }
        
        return node.executeNodeLogic(instance);
    }
}
