package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.OnFormSubmittedTrigger;
import com.operimus.Marketing.entities.WorkflowInstance;
import com.operimus.Marketing.dto.EventDTO;
import org.springframework.stereotype.Service;

@Service
public class OnFormSubmittedTriggerHandler implements NodeHandler<OnFormSubmittedTrigger> {

    @Override
    public Class<OnFormSubmittedTrigger> getHandledType() {
        return OnFormSubmittedTrigger.class;
    }

    @Override
    public WorkflowInstance execute(OnFormSubmittedTrigger node, WorkflowInstance instance) {
        return node.executeNodeLogic(instance);
    }
}
