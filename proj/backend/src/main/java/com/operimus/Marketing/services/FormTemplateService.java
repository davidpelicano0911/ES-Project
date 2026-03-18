package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.FormTemplate;
import com.operimus.Marketing.repositories.FormTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FormTemplateService {
    @Autowired
    private FormTemplateRepository formTemplateRepository;

    public FormTemplate createFormTemplate(FormTemplate formTemplate) {
        return formTemplateRepository.save(formTemplate);
    }

    public List<FormTemplate> getAllFormTemplates() {
        return formTemplateRepository.findAll();
    }

    public FormTemplate getFormTemplate(Long id) {
        return formTemplateRepository.findById(id).orElse(null);
    }

    public FormTemplate updateFormTemplate(Long id, FormTemplate request) {
        FormTemplate existingTemplate = formTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Form template not found"));

        existingTemplate.setName(request.getName());
        existingTemplate.setDescription(request.getDescription());
        existingTemplate.setFormJson(request.getFormJson());
        existingTemplate.setIsPublished(request.getIsPublished());

        return formTemplateRepository.save(existingTemplate);
    }

    public void deleteFormTemplate(Long id) {
        formTemplateRepository.deleteById(id);
    }
}
