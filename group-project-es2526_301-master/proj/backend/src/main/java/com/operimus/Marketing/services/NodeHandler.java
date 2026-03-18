package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.entities.WorkflowInstance;

public interface NodeHandler<T extends Node> {
    WorkflowInstance execute(T node, WorkflowInstance instance);
    Class<T> getHandledType();
}
