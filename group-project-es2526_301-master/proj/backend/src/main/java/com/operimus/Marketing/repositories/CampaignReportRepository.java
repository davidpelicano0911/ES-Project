package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.CampaignReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignReportRepository extends JpaRepository<CampaignReport, Long> {
}