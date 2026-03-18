package com.operimus.Marketing.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.operimus.Marketing.entities.DelayedExecution;
import com.operimus.Marketing.entities.DelayedExecution.DelayedExecutionStatus;
import com.operimus.Marketing.repository.DelayedExecutionRepository;
import com.operimus.Marketing.repositories.WorkflowInstanceRepository;
import com.operimus.Marketing.repositories.NodeRepository;
import com.operimus.Marketing.entities.WorkflowInstance;
import com.operimus.Marketing.entities.Node;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DelaySchedulerService {
    
    @Autowired
    private DelayedExecutionRepository delayedExecutionRepository;
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    @Autowired
    private NodeRepository nodeRepository;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * Schedule a delayed execution
     */
    public void scheduleDelayedExecution(Long workflowInstanceId, Long nodeId, LocalDateTime executionTime) {
        DelayedExecution delayedExecution = new DelayedExecution();
        delayedExecution.setWorkflowInstanceId(workflowInstanceId);
        delayedExecution.setNodeId(nodeId);
        delayedExecution.setExecutionTime(executionTime);
        delayedExecution.setStatus(DelayedExecutionStatus.PENDING);
        
        delayedExecutionRepository.save(delayedExecution);
        
        System.out.println("Scheduled delayed execution for workflow instance " + workflowInstanceId + 
                          " node " + nodeId + " at " + executionTime);
    }
    
    /**
     * Process pending delayed executions every minute
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processDelayedExecutions() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<DelayedExecution> readyExecutions = delayedExecutionRepository
                .findExecutionsReadyToRun(currentTime, DelayedExecutionStatus.PENDING);
        
        for (DelayedExecution execution : readyExecutions) {
            try {
                executeDelayedWorkflow(execution);
                execution.setStatus(DelayedExecutionStatus.EXECUTED);
                execution.setExecutedAt(LocalDateTime.now());
                
            } catch (Exception e) {
                System.err.println("Failed to execute delayed workflow: " + e.getMessage());
                execution.setStatus(DelayedExecutionStatus.FAILED);
            }
            
            delayedExecutionRepository.save(execution);
        }
        
        if (!readyExecutions.isEmpty()) {
            System.out.println("Processed " + readyExecutions.size() + " delayed executions");
        }
    }
    
    private void executeDelayedWorkflow(DelayedExecution execution) {
        Optional<WorkflowInstance> instanceOpt = workflowInstanceRepository.findById(execution.getWorkflowInstanceId());
        Optional<Node> nodeOpt = nodeRepository.findById(execution.getNodeId());
        
        if (instanceOpt.isPresent() && nodeOpt.isPresent()) {
            WorkflowInstance instance = instanceOpt.get();
            Node node = nodeOpt.get();
            
            System.out.println("Executing delayed workflow for instance " + instance.getId() + 
                              " from node " + node.getId());
            
            // Continue workflow execution from the delay node's outgoing nodes
            instance.getActiveNodes().addAll(node.getOutgoingNodes());
            
            // Use ApplicationContext to get WorkflowEngine bean to avoid circular dependency
            WorkflowEngine workflowEngine = applicationContext.getBean(WorkflowEngine.class);
            workflowEngine.runWorkflow(instance);
        } else {
            throw new IllegalStateException("Workflow instance or node not found for delayed execution");
        }
    }
    
    /**
     * Cancel all pending delayed executions for a workflow instance
     */
    public void cancelDelayedExecutions(Long workflowInstanceId) {
        List<DelayedExecution> pendingExecutions = delayedExecutionRepository
                .findByWorkflowInstanceIdAndStatus(workflowInstanceId, DelayedExecutionStatus.PENDING);
        
        pendingExecutions.forEach(execution -> execution.setStatus(DelayedExecutionStatus.CANCELLED));
        delayedExecutionRepository.saveAll(pendingExecutions);
        
        System.out.println("Cancelled " + pendingExecutions.size() + " pending delayed executions for workflow instance " + workflowInstanceId);
    }
}