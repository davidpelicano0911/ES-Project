package com.operimus.Marketing.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.CampaignStatus;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
public class CampaignRepositoryTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignReportRepository campaignReportRepository;

    @BeforeEach
    void cleanUp() {
        campaignRepository.deleteAll();
        campaignReportRepository.deleteAll();
    }

    @Test
    void whenFindById_thenReturnCampaign() {
        Campaign campaign = new Campaign();
        campaign.setName("Test Campaign");
        campaign.setDescription("This is a test campaign.");
        entityManager.persist(campaign);
        entityManager.flush();

        Campaign found = campaignRepository.findById(campaign.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(campaign.getName(), found.getName());
        assertEquals(campaign.getDescription(), found.getDescription());
    }

    @Test
    void whenSave_thenCampaignIsPersisted() {
        Campaign campaign = new Campaign();
        campaign.setName("New Campaign");
        campaign.setDescription("This is a new campaign.");

        Campaign savedCampaign = campaignRepository.save(campaign);

        assertNotNull(savedCampaign.getId());
        assertEquals("New Campaign", savedCampaign.getName());
    }

    @Test
    void whenDelete_thenCampaignIsRemoved() {
        Campaign campaign = new Campaign();
        campaign.setName("Test Campaign");
        campaign.setDescription("This is a test campaign.");
        entityManager.persist(campaign);
        entityManager.flush();

        campaignRepository.delete(campaign);
        entityManager.flush();

        Campaign found = campaignRepository.findById(campaign.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void whenFindByStatus_thenReturnCampaigns() {
        Campaign campaign1 = new Campaign();
        campaign1.setName("Test Campaign");
        campaign1.setDescription("This is a test campaign.");
        campaign1.setStatus(CampaignStatus.ACTIVE);
        entityManager.persist(campaign1);
        Campaign campaign2 = new Campaign();
        campaign2.setName("Another Campaign");
        campaign2.setDescription("This is another test campaign.");
        campaign2.setStatus(CampaignStatus.IN_PROGRESS);
        entityManager.persist(campaign2);
        entityManager.flush();

        List<Campaign> foundCampaigns = campaignRepository.findByStatus(CampaignStatus.ACTIVE);
        assertEquals(1, foundCampaigns.size());
        assertEquals("Test Campaign", foundCampaigns.get(0).getName());
        assertEquals(CampaignStatus.ACTIVE, foundCampaigns.get(0).getStatus());
    }

    @Test
    void whenUpdateCampaign_thenChangesArePersisted() {
        Campaign campaign = new Campaign();
        campaign.setName("Test Campaign");
        campaign.setDescription("This is a test campaign.");
        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        entityManager.persist(campaign);
        entityManager.flush();

        campaign.setStatus(CampaignStatus.FINISHED);
        Campaign updatedCampaign = campaignRepository.save(campaign);

        assertEquals(CampaignStatus.FINISHED, updatedCampaign.getStatus());
    }

    @Test
    void whenFindAll_thenReturnAllCampaigns() {
        Campaign campaign1 = new Campaign();
        campaign1.setName("Test Campaign");
        campaign1.setDescription("This is a test campaign.");
        entityManager.persist(campaign1);
        Campaign campaign2 = new Campaign();
        campaign2.setName("Another Campaign");
        campaign2.setDescription("This is another test campaign.");
        entityManager.persist(campaign2);
        entityManager.flush();

        List<Campaign> campaigns = campaignRepository.findAll();

        assertEquals(2, campaigns.size());
    }
}
