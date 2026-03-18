package com.operimus.Marketing.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DelayAction extends Node {
    @JsonProperty("delayDuration")
    private Integer delayDuration;
    
    @JsonProperty("delayUnit") 
    private String delayUnit; // "MINUTES", "HOURS", "DAYS", "WEEKS"
}