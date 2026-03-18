package com.operimus.Marketing.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.operimus.Marketing.entities.FormSubmission;

import jakarta.persistence.EntityManager;

@DataJpaTest
public class FormSubmissionRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    @BeforeEach
    void cleanUp() {
        formSubmissionRepository.deleteAll();
    }

    @Test
    void whenFindById_thenReturnFormSubmission() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{\"name\":\"John\"}");
        entityManager.persist(submission);
        entityManager.flush();

        FormSubmission found = formSubmissionRepository.findById(submission.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(submission.getFormId(), found.getFormId());
        assertEquals(submission.getLeadId(), found.getLeadId());
        assertEquals(submission.getResponsesJson(), found.getResponsesJson());
    }

    @Test
    void whenSave_thenFormSubmissionIsPersisted() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(2L);
        submission.setLeadId(102L);
        submission.setResponsesJson("{\"email\":\"john@example.com\"}");

        FormSubmission saved = formSubmissionRepository.save(submission);

        assertNotNull(saved.getId());
        assertEquals(2L, saved.getFormId());
        assertEquals(102L, saved.getLeadId());
    }

    @Test
    void whenDelete_thenFormSubmissionIsRemoved() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(3L);
        submission.setLeadId(103L);
        submission.setResponsesJson("{}");
        entityManager.persist(submission);
        entityManager.flush();

        formSubmissionRepository.delete(submission);
        entityManager.flush();

        FormSubmission found = formSubmissionRepository.findById(submission.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void whenFindByFormId_thenReturnSubmissionsWithThatFormId() {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");
        entityManager.persist(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(1L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{}");
        entityManager.persist(submission2);

        FormSubmission submission3 = new FormSubmission();
        submission3.setFormId(2L);
        submission3.setLeadId(103L);
        submission3.setResponsesJson("{}");
        entityManager.persist(submission3);

        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findByFormId(1L);

        assertEquals(2, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> s.getFormId().equals(1L)));
    }

    @Test
    void whenFindByFormId_withNoResults_thenReturnEmptyList() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{}");
        entityManager.persist(submission);
        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findByFormId(999L);

        assertTrue(submissions.isEmpty());
    }

    @Test
    void whenFindByCampaignId_thenReturnSubmissionsWithThatCampaignId() {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setCampaignId(201L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");
        entityManager.persist(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setCampaignId(201L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{}");
        entityManager.persist(submission2);

        FormSubmission submission3 = new FormSubmission();
        submission3.setFormId(3L);
        submission3.setCampaignId(202L);
        submission3.setLeadId(103L);
        submission3.setResponsesJson("{}");
        entityManager.persist(submission3);

        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findByCampaignId(201L);

        assertEquals(2, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> s.getCampaignId().equals(201L)));
    }

    @Test
    void whenFindByCampaignId_withNoResults_thenReturnEmptyList() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setCampaignId(201L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{}");
        entityManager.persist(submission);
        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findByCampaignId(999L);

        assertTrue(submissions.isEmpty());
    }

    @Test
    void whenFindByLeadId_thenReturnSubmissionsWithThatLeadId() {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");
        entityManager.persist(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(101L);
        submission2.setResponsesJson("{}");
        entityManager.persist(submission2);

        FormSubmission submission3 = new FormSubmission();
        submission3.setFormId(3L);
        submission3.setLeadId(102L);
        submission3.setResponsesJson("{}");
        entityManager.persist(submission3);

        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findByLeadId(101L);

        assertEquals(2, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> s.getLeadId().equals(101L)));
    }

    @Test
    void whenFindByLeadId_withNoResults_thenReturnEmptyList() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{}");
        entityManager.persist(submission);
        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findByLeadId(999L);

        assertTrue(submissions.isEmpty());
    }

    @Test
    void whenFindAll_thenReturnAllSubmissions() {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");
        entityManager.persist(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{}");
        entityManager.persist(submission2);

        FormSubmission submission3 = new FormSubmission();
        submission3.setFormId(3L);
        submission3.setLeadId(103L);
        submission3.setResponsesJson("{}");
        entityManager.persist(submission3);

        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findAll();

        assertEquals(3, submissions.size());
    }

    @Test
    void whenFindAll_withNoData_thenReturnEmptyList() {
        List<FormSubmission> submissions = formSubmissionRepository.findAll();

        assertTrue(submissions.isEmpty());
    }

    @Test
    void whenSaveMultiple_thenAllArePersisted() {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{}");

        formSubmissionRepository.save(submission1);
        formSubmissionRepository.save(submission2);

        List<FormSubmission> submissions = formSubmissionRepository.findAll();

        assertEquals(2, submissions.size());
        assertTrue(submissions.stream().anyMatch(s -> s.getFormId().equals(1L)));
        assertTrue(submissions.stream().anyMatch(s -> s.getFormId().equals(2L)));
    }

    @Test
    void whenUpdate_thenChangesArePersisted() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{\"name\":\"John\"}");
        entityManager.persist(submission);
        entityManager.flush();

        Long submissionId = submission.getId();

        submission.setLeadId(102L);
        submission.setResponsesJson("{\"name\":\"Jane\"}");
        formSubmissionRepository.save(submission);
        entityManager.flush();

        FormSubmission updated = formSubmissionRepository.findById(submissionId).orElse(null);

        assertNotNull(updated);
        assertEquals(102L, updated.getLeadId());
        assertEquals("{\"name\":\"Jane\"}", updated.getResponsesJson());
    }

    @Test
    void whenSubmissionCreated_thenSubmittedAtIsSet() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{}");
        entityManager.persist(submission);
        entityManager.flush();

        FormSubmission found = formSubmissionRepository.findById(submission.getId()).orElse(null);

        assertNotNull(found);
        assertNotNull(found.getSubmittedAt());
    }

    @Test
    void whenFindByFormId_withMultipleMatches_thenReturnAll() {
        // Create 5 submissions for same form
        for (int i = 1; i <= 5; i++) {
            FormSubmission submission = new FormSubmission();
            submission.setFormId(1L);
            submission.setLeadId((long) (100 + i));
            submission.setResponsesJson("{\"index\":" + i + "}");
            entityManager.persist(submission);
        }
        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findByFormId(1L);

        assertEquals(5, submissions.size());
    }

    @Test
    void whenDeleteAll_thenAllSubmissionsAreRemoved() {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");
        entityManager.persist(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(2L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{}");
        entityManager.persist(submission2);

        entityManager.flush();

        formSubmissionRepository.deleteAll();
        entityManager.flush();

        List<FormSubmission> submissions = formSubmissionRepository.findAll();

        assertTrue(submissions.isEmpty());
    }

    @Test
    void whenSaveSubmissionWithCampaignId_thenCampaignIdIsPersisted() {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setCampaignId(201L);
        submission.setLeadId(101L);
        submission.setResponsesJson("{}");

        FormSubmission saved = formSubmissionRepository.save(submission);

        assertNotNull(saved.getId());
        assertEquals(201L, saved.getCampaignId());
    }

    @Test
    void whenSaveSubmissionWithComplexJson_thenJsonIsPreserved() {
        String complexJson = "{\"name\":\"John\",\"email\":\"john@example.com\",\"preferences\":[\"email\",\"sms\"]}";
        
        FormSubmission submission = new FormSubmission();
        submission.setFormId(1L);
        submission.setLeadId(101L);
        submission.setResponsesJson(complexJson);

        FormSubmission saved = formSubmissionRepository.save(submission);
        FormSubmission found = formSubmissionRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(complexJson, found.getResponsesJson());
    }

    @Test
    void whenFindByFormIdAndLeadId_thenBothFieldsCanBeDifferent() {
        FormSubmission submission1 = new FormSubmission();
        submission1.setFormId(1L);
        submission1.setLeadId(101L);
        submission1.setResponsesJson("{}");
        entityManager.persist(submission1);

        FormSubmission submission2 = new FormSubmission();
        submission2.setFormId(1L);
        submission2.setLeadId(102L);
        submission2.setResponsesJson("{}");
        entityManager.persist(submission2);

        entityManager.flush();

        List<FormSubmission> byForm = formSubmissionRepository.findByFormId(1L);
        List<FormSubmission> byLead = formSubmissionRepository.findByLeadId(101L);

        assertEquals(2, byForm.size());
        assertEquals(1, byLead.size());
        assertEquals(101L, byLead.get(0).getLeadId());
    }
}
