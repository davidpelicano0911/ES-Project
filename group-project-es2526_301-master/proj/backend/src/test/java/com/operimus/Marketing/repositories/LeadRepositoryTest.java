package com.operimus.Marketing.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.operimus.Marketing.entities.Lead;

import jakarta.persistence.EntityManager;

@DataJpaTest
public class LeadRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LeadRepository leadRepository;

    @BeforeEach
    void cleanUp() {
        leadRepository.deleteAll();
    }

    @Test
    void whenFindById_thenReturnLead() {
        Lead lead = new Lead();
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setEmail("john.doe@example.com");
        entityManager.persist(lead);
        entityManager.flush();

        Lead found = leadRepository.findById(lead.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(lead.getFirstName(), found.getFirstName());
        assertEquals(lead.getLastName(), found.getLastName());
        assertEquals(lead.getEmail(), found.getEmail());
    }

    @Test
    void whenSave_thenLeadIsPersisted() {
        Lead lead = new Lead();
        lead.setFirstName("Alice");
        lead.setLastName("Smith");
        lead.setEmail("alice@example.com");

        Lead savedLead = leadRepository.save(lead);

        assertNotNull(savedLead.getId());
        assertEquals("Alice", savedLead.getFirstName());
    }

    @Test
    void whenDelete_thenLeadIsRemoved() {
        Lead lead = new Lead();
        lead.setFirstName("Bob");
        lead.setLastName("Brown");
        lead.setEmail("bob@example.com");
        entityManager.persist(lead);
        entityManager.flush();

        leadRepository.delete(lead);
        entityManager.flush();

        Lead found = leadRepository.findById(lead.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void whenUpdateLead_thenChangesArePersisted() {
        Lead lead = new Lead();
        lead.setFirstName("Charlie");
        lead.setLastName("Davis");
        lead.setEmail("charlie@example.com");
        entityManager.persist(lead);
        entityManager.flush();

        lead.setLastName("DavisUpdated");
        lead.setEmail("charlie.updated@example.com");
        Lead updatedLead = leadRepository.save(lead);

        assertEquals("DavisUpdated", updatedLead.getLastName());
        assertEquals("charlie.updated@example.com", updatedLead.getEmail());
    }

    @Test
    void whenFindAll_thenReturnAllLeads() {
        Lead lead1 = new Lead();
        lead1.setFirstName("Eve");
        lead1.setLastName("White");
        lead1.setEmail("eve@example.com");
        entityManager.persist(lead1);

        Lead lead2 = new Lead();
        lead2.setFirstName("Frank");
        lead2.setLastName("Black");
        lead2.setEmail("frank@example.com");
        entityManager.persist(lead2);
        entityManager.flush();

        List<Lead> leads = leadRepository.findAll();

        assertEquals(2, leads.size());
    }

    @Test
    void whenEmailExists_thenReturnTrue() {
        Lead lead = new Lead();
        lead.setEmail("test@example.com");
        lead.setFirstName("Test");
        lead.setLastName("User");
        entityManager.persist(lead);
        entityManager.flush();

        boolean exists = leadRepository.existsByEmail("test@example.com");
        assertTrue(exists);
        
        boolean notExists = leadRepository.existsByEmail("nonexistent@example.com");
        assertFalse(notExists);
    }

    @Test
    void whenPhoneNumberExists_thenReturnTrue() {
        Lead lead = new Lead();
        lead.setPhoneNumber("+1234567890");
        lead.setEmail("test@example.com");
        lead.setFirstName("Test");
        lead.setLastName("User");
        entityManager.persist(lead);
        entityManager.flush();

        boolean exists = leadRepository.existsByPhoneNumber("+1234567890");
        assertTrue(exists);

        boolean notExists = leadRepository.existsByPhoneNumber("+0987654321");
        assertFalse(notExists);
    }

    @Test
    void whenFindByCrmId_thenReturnLead() {
        Lead lead = new Lead();
        lead.setFirstName("CRM");
        lead.setLastName("User");
        lead.setEmail("crm@example.com");
        lead.setCrmId("CRM123");
        entityManager.persist(lead);
        entityManager.flush();

        Lead found = leadRepository.findByCrmId("CRM123").orElse(null);

        assertNotNull(found);
        assertEquals("CRM123", found.getCrmId());
        assertEquals("crm@example.com", found.getEmail());
    }

    @Test
    void whenFindByCrmId_withNonexistentId_thenReturnEmpty() {
        Optional<Lead> result = leadRepository.findByCrmId("NON_EXISTENT");
        assertTrue(result.isEmpty());
    }

    @Test
    void whenFindLatestSyncDate_thenReturnMostRecentDate() throws InterruptedException {
        Lead lead1 = new Lead();
        lead1.setFirstName("Sync1");
        lead1.setLastName("User1");
        lead1.setEmail("sync1@example.com");
        lead1.setLastSyncedAt(new Date(System.currentTimeMillis()));
        entityManager.persist(lead1);

        Thread.sleep(10); // ensure different timestamps

        Lead lead2 = new Lead();
        lead2.setFirstName("Sync2");
        lead2.setLastName("User2");
        lead2.setEmail("sync2@example.com");
        lead2.setLastSyncedAt(new Date(System.currentTimeMillis()));
        entityManager.persist(lead2);

        entityManager.flush();

        Date latest = leadRepository.findLatestSyncDate();

        assertNotNull(latest);
        assertEquals(lead2.getLastSyncedAt(), latest);
    }

    @Test
    void whenNoLeadsExist_findLatestSyncDateReturnsNull() {
        Date latest = leadRepository.findLatestSyncDate();
        assertNull(latest);
    }
}
