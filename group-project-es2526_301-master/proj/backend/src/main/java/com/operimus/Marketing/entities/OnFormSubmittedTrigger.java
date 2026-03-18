package com.operimus.Marketing.entities;

import com.operimus.Marketing.dto.EventDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Objects;



@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class OnFormSubmittedTrigger extends Node {
    @JsonProperty("formId")
    private Long formId;

    @Override
    public boolean matchesEvent(EventDTO event) {
        System.out.println("Matching event in OnFormSubmittedTrigger for formId: " + formId);

        if (event.getMetadata().containsKey("formId")) {
            Object raw = event.getMetadataValue("formId");
            System.out.println("Event metadata contains formId: " + raw);

            // safe conversion
            Long metaFormId = raw == null ? null : Long.valueOf(raw.toString());
            boolean match = Objects.equals(formId, metaFormId);

            System.out.println("Comparison result: " + match);
            return match;
        }

        System.out.println("Event metadata does not contain formId");
        return false;
    }
    
}