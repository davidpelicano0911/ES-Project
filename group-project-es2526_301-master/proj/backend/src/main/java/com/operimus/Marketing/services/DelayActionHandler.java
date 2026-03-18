package com.operimus.Marketing.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.operimus.Marketing.entities.DelayAction;
import com.operimus.Marketing.entities.WorkflowInstance;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class DelayActionHandler implements NodeHandler<DelayAction> {

    @Autowired
    private DelaySchedulerService delaySchedulerService;

    @Override
    public Class<DelayAction> getHandledType() {
        return DelayAction.class;
    }

    @Override
    public WorkflowInstance execute(DelayAction node, WorkflowInstance instance) {
        System.out.println("Executing DelayAction node ID: " + node.getId() + 
                          " with duration: " + node.getDelayDuration() + " " + node.getDelayUnit());
        
        // Calculate execution time
        LocalDateTime executionTime = LocalDateTime.now().plusSeconds(
            convertToSeconds(node.getDelayDuration(), node.getDelayUnit())
        );
        
        // Schedule the delayed execution
        delaySchedulerService.scheduleDelayedExecution(
            instance.getId(),
            node.getId(), 
            executionTime
        );
        
        // Mark this node as completed but don't continue to next nodes immediately
        // The scheduler will continue the workflow later
        instance.getCompletedNodeIds().add(node.getId());
        
        System.out.println("Delay scheduled for " + executionTime + 
                          ". Workflow will continue automatically after delay.");
        
        return instance;
    }
    
    private long convertToSeconds(Integer duration, String unit) {
        if (duration == null || duration <= 0) {
            return 0;
        }
        
        return switch (unit != null ? unit.toUpperCase() : "MINUTES") {
            case "SECONDS" -> duration;
            case "MINUTES" -> TimeUnit.MINUTES.toSeconds(duration);
            case "HOURS" -> TimeUnit.HOURS.toSeconds(duration);
            case "DAYS" -> TimeUnit.DAYS.toSeconds(duration);
            default -> TimeUnit.MINUTES.toSeconds(duration); // Default to minutes
        };
    }
}