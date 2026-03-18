package com.operimus.Marketing.dto;

import com.operimus.Marketing.entities.EventType;
import java.util.Map;
import lombok.Data;

@Data
public class EventDTO {

    private EventType eventType;
    
    private Long leadId;

    private Map<String, Object> metadata;

    public Object getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
}