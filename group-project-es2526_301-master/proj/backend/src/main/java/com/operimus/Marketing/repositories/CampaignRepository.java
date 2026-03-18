package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.CampaignStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(CampaignStatus status);
}

