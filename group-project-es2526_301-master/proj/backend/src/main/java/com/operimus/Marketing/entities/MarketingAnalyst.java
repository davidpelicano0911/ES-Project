package com.operimus.Marketing.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MARKETING_ANALYST")
public class MarketingAnalyst extends User {
    public MarketingAnalyst() {
        super();
    }

    public MarketingAnalyst(String keycloakId, String name, String email, LocalDate birthDate) {
        super(keycloakId, name, email, birthDate);
    }
}
