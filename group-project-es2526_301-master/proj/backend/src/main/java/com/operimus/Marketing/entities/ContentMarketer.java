package com.operimus.Marketing.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("CONTENT_MARKETER")
public class ContentMarketer extends User {
    public ContentMarketer() {
        super();
    }

    public ContentMarketer(String keycloakId, String name, String email, LocalDate birthDate) {
        super(keycloakId, name, email, birthDate);
    }
}
