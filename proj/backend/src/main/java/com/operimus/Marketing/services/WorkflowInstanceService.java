package com.operimus.Marketing.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.operimus.Marketing.repositories.WorkflowInstanceRepository;

@Service
public class WorkflowInstanceService {
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    public java.util.List<com.operimus.Marketing.entities.WorkflowInstance> getAllInstances() {
        return workflowInstanceRepository.findAll();
    }

    public java.util.List<com.operimus.Marketing.entities.WorkflowInstance> getInstancesByWorkflowId(Long workflowId) {
        if (workflowId == null) return java.util.Collections.emptyList();
        return workflowInstanceRepository.findByWorkflowId(workflowId);
    }
}
