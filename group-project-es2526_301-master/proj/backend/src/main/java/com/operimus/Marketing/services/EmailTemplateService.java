package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.EmailTemplate;
import com.operimus.Marketing.repositories.EmailTemplateRepository;
import com.operimus.Marketing.repositories.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.operimus.Marketing.entities.Lead;
import java.util.List;
import java.util.Optional;

@Service
public class EmailTemplateService {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MailJetService mailJetService;

    @Autowired
    private EmailLogService emailLogService;

    @Autowired
    private LeadService leadService;

    public EmailTemplate createTemplate(EmailTemplate template) {
        if (emailTemplateRepository.existsByName(template.getName())) {
            throw new IllegalArgumentException("Template name already exists");
        }
        return emailTemplateRepository.save(template);
    }

    public Optional<EmailTemplate> getTemplateById(Long id) {
        return emailTemplateRepository.findById(id);
    }

    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAll();
    }

    public EmailTemplate updateTemplate(Long id, EmailTemplate updated) {
        EmailTemplate existing = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getSubject() != null) existing.setSubject(updated.getSubject());
        if (updated.getBody() != null) existing.setBody(updated.getBody());
        if (updated.getDesign() != null) existing.setDesign(updated.getDesign());

        return emailTemplateRepository.save(existing);
    }

    public void deleteTemplate(Long id) {
        emailTemplateRepository.deleteById(id);
    }

    
    public void test_emailTemplate(Long id, String testEmail) {
        if (testEmail == null || testEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Test email address is required");
        }

        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        // Create a temporary lead object for the test email
        Lead testLead = new Lead();
        testLead.setEmail(testEmail);
        testLead.setFirstName("Test");
        testLead.setLastName("User");

        Long messageId = mailJetService.sendEmail(testLead, "ferreira.manuel.henrique04@gmail.com", convertPlaceholders(template.getSubject()), convertPlaceholders(template.getBody()));
        
        // Log the test email - try to find the lead by email if it exists
        if (messageId != null && messageId > 0) {
            Long leadId = null;
            try {
                // Check if this email exists in the leads table
                Long foundLeadId = leadService.getLeadIdByEmail(testEmail);
                if (foundLeadId != null && foundLeadId > 0) {
                    leadId = foundLeadId;
                }
            } catch (Exception e) {
                // Lead doesn't exist, that's fine - leave leadId as null
            }
            
            emailLogService.logEmail(null, leadId, testEmail, messageId, template.getSubject());
        }
    }
    
    private String convertPlaceholders(String body) {
        return body.replaceAll("\\{\\{\\s*(.*?)\\s*\\}\\}", "{{var:$1}}");
    }
    
}
