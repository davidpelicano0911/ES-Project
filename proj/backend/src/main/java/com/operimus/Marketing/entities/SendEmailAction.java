package com.operimus.Marketing.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class SendEmailAction extends Node {
    @JsonProperty("emailTemplateId")
    private Long emailTemplateId;
    @JsonProperty("sendFrom")
    private String sendFrom;
}