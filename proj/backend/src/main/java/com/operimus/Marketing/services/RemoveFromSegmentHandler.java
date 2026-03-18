package com.operimus.Marketing.services;


import org.springframework.stereotype.Service;

import com.operimus.Marketing.entities.WorkflowInstance;
import com.operimus.Marketing.services.NodeHandler;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.entities.RemoveFromSegmentAction;

@Service
public class RemoveFromSegmentHandler implements NodeHandler<RemoveFromSegmentAction>  {

    @Override
    public Class <RemoveFromSegmentAction> getHandledType() {
        return RemoveFromSegmentAction.class;
    }

    @Override
    public WorkflowInstance execute(RemoveFromSegmentAction node, WorkflowInstance instance) {
        System.out.println("Executing RemoveFromSegmentHandler for instance: " + instance.getId());

        Long segmentId = node.getSegmentId();
        
        Lead lead = instance.getLead();
        lead.removeSegment(segmentId);

        return node.executeNodeLogic(instance);
    }
}