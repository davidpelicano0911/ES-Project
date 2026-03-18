package com.operimus.Marketing.component;

import org.hibernate.annotations.SourceType;
import org.springframework.stereotype.Component;
import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.entities.OnFormSubmittedTrigger;
import com.operimus.Marketing.entities.SendEmailAction;
import com.operimus.Marketing.entities.AddToSegmentAction;
import com.operimus.Marketing.entities.DelayAction;
import com.operimus.Marketing.entities.RemoveFromSegmentAction;


@Component
public class NodeFactory {

    public Node createNode(Node node) {
        Node newNode;

        if (node.getEventType() == null) {
            newNode = new Node();
        }else {
            switch (node.getEventType()) {
                case FORM_SUBMITTED -> {
                    newNode = new OnFormSubmittedTrigger();
                    if (node instanceof OnFormSubmittedTrigger sourceFormNode) {
                        ((OnFormSubmittedTrigger) newNode).setFormId(sourceFormNode.getFormId());
                    }
                    
                }
                case SEND_EMAIL -> {
                    newNode = new SendEmailAction();
                    System.out.println("Creating SendEmailAction node");
                    if (node instanceof SendEmailAction sourceEmailNode) {
                        System.out.println("Copying SendEmailAction properties");
                        System.out.println("Source Email Template ID: " + sourceEmailNode.getEmailTemplateId());
                        ((SendEmailAction) newNode).setEmailTemplateId(sourceEmailNode.getEmailTemplateId());
                        ((SendEmailAction) newNode).setSendFrom(sourceEmailNode.getSendFrom());
                    }

                }
                case ADD_TO_SEGMENT -> {
                    newNode = new AddToSegmentAction();
                    if (node instanceof AddToSegmentAction sourceSegmentNode) {
                        ((AddToSegmentAction) newNode).setSegmentId(sourceSegmentNode.getSegmentId());
                    }
                }
                case REMOVE_FROM_SEGMENT -> {
                    newNode = new RemoveFromSegmentAction();
                    if (node instanceof RemoveFromSegmentAction sourceRemoveSegmentNode) {
                        ((RemoveFromSegmentAction) newNode).setSegmentId(sourceRemoveSegmentNode.getSegmentId());
                    }
                }
                case DELAY -> {
                    newNode = new DelayAction();
                    if (node instanceof DelayAction sourceDelayNode) {
                        ((DelayAction) newNode).setDelayDuration(sourceDelayNode.getDelayDuration());
                        ((DelayAction) newNode).setDelayUnit(sourceDelayNode.getDelayUnit());
                    }
                }
                // Add more cases here for other triggers/actions
                default -> newNode = new Node();
            }
        }   
        

        newNode.setPositionX(node.getPositionX());
        newNode.setPositionY(node.getPositionY());
        newNode.setIsStartNode(node.getIsStartNode());
        newNode.setIsEndNode(node.getIsEndNode());
        newNode.setNodeType(node.getNodeType());
        newNode.setEventType(node.getEventType());
        newNode.setLabel(node.getLabel());
        // Note: workflow is set by the caller

        return newNode;
    }

    public Node updateNode(Node existingNode, Node updatedNode) {

        // Same type - update in place
        existingNode.setPositionX(updatedNode.getPositionX());
        existingNode.setPositionY(updatedNode.getPositionY());
        existingNode.setIsStartNode(updatedNode.getIsStartNode());
        existingNode.setIsEndNode(updatedNode.getIsEndNode());
        existingNode.setNodeType(updatedNode.getNodeType());
        existingNode.setEventType(updatedNode.getEventType());
        existingNode.setLabel(updatedNode.getLabel());

        // Update specific properties based on node type
        if (existingNode instanceof OnFormSubmittedTrigger && updatedNode instanceof OnFormSubmittedTrigger) {
            ((OnFormSubmittedTrigger) existingNode).setFormId(((OnFormSubmittedTrigger) updatedNode).getFormId());
        } else if (existingNode instanceof SendEmailAction && updatedNode instanceof SendEmailAction) {
            ((SendEmailAction) existingNode).setEmailTemplateId(((SendEmailAction) updatedNode).getEmailTemplateId());
            ((SendEmailAction) existingNode).setSendFrom(((SendEmailAction) updatedNode).getSendFrom());
        } else if (existingNode instanceof AddToSegmentAction && updatedNode instanceof AddToSegmentAction) {
            ((AddToSegmentAction) existingNode).setSegmentId(((AddToSegmentAction) updatedNode).getSegmentId());
        } else if (existingNode instanceof RemoveFromSegmentAction && updatedNode instanceof RemoveFromSegmentAction) {
            ((RemoveFromSegmentAction) existingNode).setSegmentId(((RemoveFromSegmentAction) updatedNode).getSegmentId());
        } else if (existingNode instanceof DelayAction && updatedNode instanceof DelayAction) {
            ((DelayAction) existingNode).setDelayDuration(((DelayAction) updatedNode).getDelayDuration());
            ((DelayAction) existingNode).setDelayUnit(((DelayAction) updatedNode).getDelayUnit());
        }

        return existingNode;
    }
}
