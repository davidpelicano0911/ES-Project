package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.LandingPageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandingPageEventRepository extends JpaRepository<LandingPageEvent, Long> {
    List<LandingPageEvent> findByLandingPageId(Long landingPageId);
    List<LandingPageEvent> findByLeadId(Long leadId);
}
