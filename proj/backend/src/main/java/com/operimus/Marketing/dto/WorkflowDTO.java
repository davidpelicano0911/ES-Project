package com.operimus.Marketing.dto;

import lombok.Data;

@Data
public class WorkflowDTO {
    private String name;
    private String description;
    private String createdAt;
    private Long campaignId;
}