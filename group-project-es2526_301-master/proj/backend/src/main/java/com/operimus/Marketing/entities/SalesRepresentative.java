package com.operimus.Marketing.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("SALES_REPRESENTATIVE")
public class SalesRepresentative extends User {
    public SalesRepresentative() {
        super();
    }

    public SalesRepresentative(String keycloakId, String name, String email, LocalDate birthDate) {
        super(keycloakId, name, email, birthDate);
    }
}
