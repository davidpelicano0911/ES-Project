package com.operimus.Marketing.init;

import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.entities.Workflow;

import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.SegmentsRepository;
import com.operimus.Marketing.repositories.WorkflowRepository;

import com.operimus.Marketing.services.MarketingIndexerService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class IndexInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializer.class);

    private final MarketingIndexerService indexerService;
    private final CampaignRepository campaignRepository;
    private final SegmentsRepository segmentsRepository;
    private final WorkflowRepository workflowRepository;

    public IndexInitializer(
        MarketingIndexerService indexerService,
        CampaignRepository campaignRepository,
        SegmentsRepository segmentsRepository,
        WorkflowRepository workflowRepository
    ) {
        this.indexerService = indexerService;
        this.campaignRepository = campaignRepository;
        this.segmentsRepository = segmentsRepository;
        this.workflowRepository = workflowRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database content indexing on startup...");
        
        long startTime = System.currentTimeMillis();

        List<Campaign> campaigns = campaignRepository.findAll();
        for (Campaign campaign : campaigns) {
            indexerService.indexCampaign(campaign);
        }
        log.info("Indexed {} Campaign(s).", campaigns.size());

        List<Segments> segments = segmentsRepository.findAll();
        for (Segments segment : segments) {
            indexerService.indexSegment(segment);
        }
        log.info("Indexed {} Segment(s).", segments.size());
        
        List<Workflow> workflows = workflowRepository.findAll();
        for (Workflow workflow : workflows) {
            indexerService.indexWorkflow(workflow);
        }
        log.info("Indexed {} Workflow(s).", workflows.size());

        long endTime = System.currentTimeMillis();
        log.info("Startup indexing completed in {}ms.", (endTime - startTime));
    }
}