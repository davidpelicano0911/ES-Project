package com.operimus.Marketing.services;

import org.springframework.stereotype.Service;
import com.operimus.Marketing.entities.AddToSegmentAction;
import com.operimus.Marketing.entities.WorkflowInstance;
import com.operimus.Marketing.services.NodeHandler;
import com.operimus.Marketing.entities.Lead;

@Service
public class AddToSegmentHandler implements NodeHandler<AddToSegmentAction> {

    @Override
    public Class<AddToSegmentAction> getHandledType() {
        return AddToSegmentAction.class;
    }

    @Override
    public WorkflowInstance execute(AddToSegmentAction node, WorkflowInstance instance) {
        System.out.println("Executing AddToSegmentAction node ID: " + node.getId());
        // Logic to add the lead to the specified segment
        Long segmentId = node.getSegmentId();
        
        Lead lead = instance.getLead();
        lead.addSegment(segmentId);
        
        return node.executeNodeLogic(instance);
    }
}
