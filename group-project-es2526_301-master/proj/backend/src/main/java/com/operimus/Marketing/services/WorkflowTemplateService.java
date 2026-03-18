package com.operimus.Marketing.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.operimus.Marketing.entities.WorkflowTemplate;
import com.operimus.Marketing.repositories.WorkflowTemplateRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class WorkflowTemplateService {
    @Autowired
    private WorkflowTemplateRepository workflowTemplateRepository;

    public WorkflowTemplate createWorkflowTemplate(WorkflowTemplate template) {
        return workflowTemplateRepository.save(template);
    }

    public List<WorkflowTemplate> getAllWorkflowTemplates() {
        return workflowTemplateRepository.findAll();
    }

    public WorkflowTemplate getWorkflowTemplateById(Long id) {
        return workflowTemplateRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Template not found"));
    }

    public WorkflowTemplate updateWorkflowTemplate(Long id, WorkflowTemplate updatedTemplate) {
        WorkflowTemplate existingTemplate = workflowTemplateRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        if (updatedTemplate.getName() != null) {
            existingTemplate.setName(updatedTemplate.getName());
        }
        if (updatedTemplate.getDescription() != null) {
            existingTemplate.setDescription(updatedTemplate.getDescription());
        }
        if (updatedTemplate.getTemplateData() != null) {
            existingTemplate.setTemplateData(updatedTemplate.getTemplateData());
        }
        return workflowTemplateRepository.save(existingTemplate);
    }

    public void deleteWorkflowTemplate(Long id) {
        if (!workflowTemplateRepository.existsById(id)) {
            throw new EntityNotFoundException("Template not found");
        }
        workflowTemplateRepository.deleteById(id);
    }
}
