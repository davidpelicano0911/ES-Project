package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
    List<FormSubmission> findByFormId(Long formId);
    List<FormSubmission> findByCampaignId(Long campaignId);
    List<FormSubmission> findByLeadId(Long leadId);

    @Query("SELECT s.formId as formId, COUNT(s) as count FROM FormSubmission s GROUP BY s.formId")
    List<Object[]> countSubmissionsByForm();
}
