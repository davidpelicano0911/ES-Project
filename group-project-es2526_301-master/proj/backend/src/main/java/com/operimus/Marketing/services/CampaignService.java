package com.operimus.Marketing.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.CampaignStatus;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.entities.Dashboard;
import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.DashboardRepository;

import com.operimus.Marketing.entities.CampaignMaterials;
import com.operimus.Marketing.entities.CampaignReport;
import com.operimus.Marketing.repositories.CampaignMaterialsRepository;
import com.operimus.Marketing.repositories.CampaignReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignService {
    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private CampaignMaterialsRepository campaignMaterialsRepository;

    @Autowired
    private CampaignReportRepository campaignReportRepository;

    public Campaign createCampaign(Campaign campaign) {
        System.out.println("Creating campaign: " + campaign.getId());
        if (campaign.getCreatedAt() == null) {
            campaign.setCreatedAt(new java.util.Date());
        }
        if (campaign.getDueDate() != null && campaign.getDueDate().before(campaign.getCreatedAt())) {
            throw new IllegalArgumentException("Due date cannot be before created date");
        }
        return campaignRepository.save(campaign);
    }

    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found with id: " + id));
    }

    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    public Campaign updateCampaign(Long id, Campaign campaignDetails) {
        Campaign existing = campaignRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found with id: " + id));

        existing.setName(campaignDetails.getName());
        existing.setDescription(campaignDetails.getDescription());
        existing.setStatus(campaignDetails.getStatus());
        existing.setDueDate(campaignDetails.getDueDate());

        return campaignRepository.save(existing);
    }

    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
    }

    public List<Campaign> getCampaignsByStatus(CampaignStatus status) {
        return campaignRepository.findByStatus(status);
    }

    public Workflow getWorkflowByCampaignId(Long id) {
        Campaign campaign = campaignRepository.findById(id).orElse(null);
        if (campaign != null) {
            return campaign.getWorkflow();
        }
        return null;
    }


    public Dashboard createCampaignDashboard(Long campaign_id, Dashboard dashboard) {
        Campaign existingCampaign = campaignRepository.findById(campaign_id).orElse(null);
        if (existingCampaign != null) {
            dashboard.setCampaign(existingCampaign);
            existingCampaign.setDashboard(dashboard);
            
            dashboardRepository.save(dashboard);
            campaignRepository.save(existingCampaign);
            
            return dashboard;
        }
        return null;
    }
 
    public Dashboard getCampaignDashboard(Long campaign_id) {
        Campaign campaign = campaignRepository.findById(campaign_id).orElse(null);
        return campaign != null ? campaign.getDashboard() : null;
    }

    @Transactional
    public Dashboard updateCampaignDashboard(Long campaign_id, Dashboard dashboardIncomingData) {
        if (dashboardIncomingData == null) {
            throw new IllegalArgumentException("Dashboard data cannot be null");
        }
        Campaign existingCampaign = campaignRepository.findById(campaign_id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        Dashboard existingDashboard = existingCampaign.getDashboard();

        if (existingDashboard != null) {
            existingDashboard.setLayoutData(dashboardIncomingData.getLayoutData());

            if (dashboardIncomingData.getTitle() != null) {
                existingDashboard.setTitle(dashboardIncomingData.getTitle());
            }
            return dashboardRepository.save(existingDashboard);
        } else {
            dashboardIncomingData.setCampaign(existingCampaign);
            existingCampaign.setDashboard(dashboardIncomingData);

            dashboardRepository.save(dashboardIncomingData);
            campaignRepository.save(existingCampaign);
            
            return dashboardIncomingData;
        }
    }

    // ======================= MATERIALS MANAGEMENT ======================= //

    /**
     * Get all materials linked to a campaign
     */
    @Transactional(readOnly = true) 
    public List<CampaignMaterials> getCampaignMaterials(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Campaign not found with id: " + campaignId));

        return List.copyOf(campaign.getMaterials());
    }

    /**
     * Attach a material to a campaign
     */
    @Transactional
    public void attachMaterialToCampaign(Long campaignId, Long materialId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Campaign not found with id: " + campaignId));

        CampaignMaterials material = campaignMaterialsRepository.findById(materialId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Material not found with id: " + materialId));

        // avoid duplicates
        if (!campaign.getMaterials().contains(material)) {
            campaign.getMaterials().add(material);
            material.getCampaigns().add(campaign);
        }

        campaignMaterialsRepository.save(material);
        campaignRepository.save(campaign);
    }


    @Transactional
    public void detachMaterialFromCampaign(Long campaignId, Long materialId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        CampaignMaterials material = campaignMaterialsRepository.findById(materialId)
            .orElseThrow(() -> new RuntimeException("Material not found"));

        campaign.getMaterials().remove(material);
        material.getCampaigns().remove(campaign);

        campaignMaterialsRepository.save(material);
        campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public List<CampaignMaterials> getAllCampaignMaterials() {
        return campaignMaterialsRepository.findAll();
    }

    @Transactional
    public CampaignReport createCampaignReport(Long campaignId, String name, byte[] data) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        CampaignReport report = new CampaignReport();
        report.setName(name);
        report.setPdfData(data);
        report.setCampaign(campaign);
        
        return campaignReportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<CampaignReport> getCampaignReports(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        return campaign.getReports();
    }

    @Transactional(readOnly = true)
    public CampaignReport getReportById(Long reportId) {
        return campaignReportRepository.findById(reportId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }
}
