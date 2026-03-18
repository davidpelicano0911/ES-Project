package com.operimus.Marketing.repositories;

import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.operimus.Marketing.entities.EmailTemplate;

@DataJpaTest
public class EmailTemplateRepositoryTest {
    @Autowired
    private EntityManager entityManager;   

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @BeforeEach
    void cleanUp() {
        emailTemplateRepository.deleteAll();
    }

    @Test
    void whenFindById_thenReturnEmailTemplate() {
        EmailTemplate template = new EmailTemplate();
        template.setName("Test Template");
        template.setSubject("This is a test template.");
        entityManager.persist(template);
        entityManager.flush();

        EmailTemplate found = emailTemplateRepository.findById(template.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(template.getName(), found.getName());
        assertEquals(template.getSubject(), found.getSubject());
    }

    @Test
    void whenSave_thenEmailTemplateIsPersisted() {
        EmailTemplate template = new EmailTemplate();
        template.setName("New Template");
        template.setSubject("This is a new template."); 
        EmailTemplate savedTemplate = emailTemplateRepository.save(template);
        assertNotNull(savedTemplate.getId());
        assertEquals("New Template", savedTemplate.getName());
        assertEquals("This is a new template.", savedTemplate.getSubject());
    }

    @Test
    void whenDelete_thenEmailTemplateIsRemoved() {
        EmailTemplate template = new EmailTemplate();
        template.setName("Template to Delete");
        template.setSubject("This template will be deleted.");
        entityManager.persist(template);
        entityManager.flush();

        emailTemplateRepository.delete(template);
        EmailTemplate deleted = emailTemplateRepository.findById(template.getId()).orElse(null);

        assertEquals(null, deleted);
    }

    @Test
    void whenExistsByName_thenReturnTrue() {
        EmailTemplate template = new EmailTemplate();
        template.setName("Existing Template");
        template.setSubject("This template exists.");
        entityManager.persist(template);
        entityManager.flush();      

        boolean exists = emailTemplateRepository.existsByName("Existing Template");
        assertEquals(true, exists);
    }

    @Test
    void whenNotExistsByName_thenReturnFalse() {    
        boolean exists = emailTemplateRepository.existsByName("Nonexistent Template");
        assertEquals(false, exists);
    }

    @Test
    void whenFindAll_thenReturnAllEmailTemplates() {
        EmailTemplate template1 = new EmailTemplate();
        template1.setName("Template 1");
        template1.setSubject("First template.");
        entityManager.persist(template1);

        EmailTemplate template2 = new EmailTemplate();
        template2.setName("Template 2");
        template2.setSubject("Second template.");
        entityManager.persist(template2);

        entityManager.flush();

        var templates = emailTemplateRepository.findAll();
        assertEquals(2, templates.size());
    }
}