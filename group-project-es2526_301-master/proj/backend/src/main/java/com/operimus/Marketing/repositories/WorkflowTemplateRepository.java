package com.operimus.Marketing.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.operimus.Marketing.entities.WorkflowTemplate;

public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, Long> {
    
}
