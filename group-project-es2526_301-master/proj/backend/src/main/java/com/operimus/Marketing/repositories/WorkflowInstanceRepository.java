package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.entities.Lead;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    WorkflowInstance findByWorkflowAndLead(Workflow workflow, Lead lead);

    java.util.List<WorkflowInstance> findByWorkflowId(Long workflowId);
}