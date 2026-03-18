package com.operimus.Marketing.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MARKETING_MANAGER")
public class MarketingManager extends User {
    public MarketingManager() {
        super();
    }

    public MarketingManager(String keycloakId, String name, String email, LocalDate birthDate) {
        super(keycloakId, name, email, birthDate);
    }
}
