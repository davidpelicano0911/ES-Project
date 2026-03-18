package com.operimus.Marketing.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.CampaignMaterials;
import com.operimus.Marketing.entities.CampaignReport;
import com.operimus.Marketing.entities.CampaignStatus;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.entities.Dashboard;
import com.operimus.Marketing.services.CampaignService;
import com.operimus.Marketing.services.MarketingIndexerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api/${api.version}/campaigns")
@Tag(name = "Campaign Management", description = "Endpoints for managing marketing campaigns")
public class CampaignController {
    @Autowired
    private CampaignService campaignService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;

    @Operation(summary = "Create a new campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campaign created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Campaign.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/")
    public Campaign createCampaign(@RequestBody Campaign campaign) {
        Campaign createdCampaign = campaignService.createCampaign(campaign);
        marketingIndexerService.indexCampaign(createdCampaign);
        return createdCampaign;
    }

    @Operation(summary = "Get all campaigns")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaigns", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Campaign.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/")
    public List<Campaign> getAllCampaigns() {
        return campaignService.getAllCampaigns();
    }

    @Operation(summary = "Get campaigns by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaigns by status", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Campaign.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/status/{campaign_status}")
    public List<Campaign> getCampaignsByStatus(@Parameter(description = "Status of the campaign", example = "ACTIVE") @PathVariable CampaignStatus campaign_status) {
        return campaignService.getCampaignsByStatus(campaign_status);
    }

    @Operation(summary = "Update an existing campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campaign updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Campaign.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public Campaign updateCampaign(
        @Parameter(description = "ID of the campaign to update", example = "1") @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated campaign object",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Campaign.class)
            )
        ) @RequestBody Campaign campaign) {
        Campaign updatedCampaign = campaignService.updateCampaign(id, campaign);
        marketingIndexerService.indexCampaign(updatedCampaign);
        return updatedCampaign;
    }

    @Operation(summary = "Get a campaign by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaign", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Campaign.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public Campaign getCampaignById(@Parameter(description = "ID of the campaign to retrieve", example = "1") @PathVariable Long id) {
        return campaignService.getCampaignById(id);
    }

    @Operation(summary = "Delete a campaign by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campaign deleted successfully", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void deleteCampaign(@Parameter(description = "ID of the campaign to delete", example = "1") @PathVariable Long id) {
        marketingIndexerService.deleteFromIndex(id, "CAMPAIGN");
        campaignService.deleteCampaign(id);
    }

    @Operation(summary = "Get campaign dashboard by campaign ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaign dashboard", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dashboard.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign or dashboard not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{campaign_id}/dashboard")
    public ResponseEntity<Dashboard> getCampaignDashboard(@Parameter(description = "ID of the campaign to retrieve dashboard for", example = "1") @PathVariable Long campaign_id) {
        Dashboard dashboard = campaignService.getCampaignDashboard(campaign_id);
        return dashboard != null
                ? ResponseEntity.ok(dashboard)
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update campaign dashboard by campaign ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campaign dashboard updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dashboard.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign or dashboard not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{campaign_id}/dashboard")
    public ResponseEntity<Dashboard> updateCampaignDashboard(
        @Parameter(description = "ID of the campaign to update dashboard for", example = "1") @PathVariable Long campaign_id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dashboard object to update",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dashboard.class)
            )
        ) @RequestBody Dashboard dashboard) {

        Dashboard updated = campaignService.updateCampaignDashboard(campaign_id, dashboard);
        marketingIndexerService.indexDashboard(updated);
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create campaign dashboard by campaign ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campaign dashboard created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dashboard.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{campaign_id}/dashboard")
    public ResponseEntity<Dashboard> createCampaignDashboard(
        @Parameter(description = "ID of the campaign to create dashboard for", example = "1") @PathVariable Long campaign_id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dashboard object to create",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dashboard.class)
            )
        ) @RequestBody Dashboard dashboard) {
        Dashboard created = campaignService.createCampaignDashboard(campaign_id, dashboard);
        marketingIndexerService.indexDashboard(created);
        return created != null
                ? ResponseEntity.ok(created)
                : ResponseEntity.notFound().build(); 
    }

    @Operation(summary = "Get workflow by campaign ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaign workflow", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Workflow.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign or workflow not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}/workflow")
    public ResponseEntity<Workflow> getWorkflowByCampaignId(@Parameter(description = "ID of the campaign to retrieve workflow for", example = "1") @PathVariable Long id) {
        Workflow workflow = campaignService.getWorkflowByCampaignId(id);
        return workflow != null 
            ? ResponseEntity.ok(workflow) 
            : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get all materials linked to a campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaign materials", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CampaignMaterials.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{campaign_id}/materials")
    public List<CampaignMaterials> getCampaignMaterials(@Parameter(description = "ID of the campaign to retrieve materials for", example = "1") @PathVariable Long campaign_id) {
        return campaignService.getCampaignMaterials(campaign_id);
    }

    @Operation(summary = "Attach material to a campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material attached successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Campaign or material not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{campaign_id}/materials/{material_id}")
    public ResponseEntity<Void> attachMaterial(
            @Parameter(description = "ID of the campaign to attach material to", example = "1") @PathVariable Long campaign_id,
            @Parameter(description = "ID of the material to attach", example = "1") @PathVariable Long material_id) {
        campaignService.attachMaterialToCampaign(campaign_id, material_id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Detach material from a campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Material detached successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Campaign or material not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{campaign_id}/materials/{material_id}")
    public ResponseEntity<Void> detachMaterial(
            @Parameter(description = "ID of the campaign to detach material from", example = "1") @PathVariable Long campaign_id,
            @Parameter(description = "ID of the material to detach", example = "1") @PathVariable Long material_id) {
        campaignService.detachMaterialFromCampaign(campaign_id, material_id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all campaign materials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of all campaign materials", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CampaignMaterials.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/materials")
    public List<CampaignMaterials> getAllCampaignMaterials() {
        return campaignService.getAllCampaignMaterials();
    }

    @Operation(summary = "Get all reports linked to a campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaign reports", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CampaignReport.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{campaign_id}/reports")
    public ResponseEntity<List<CampaignReport>> getCampaignReports(@Parameter(description = "ID of the campaign to retrieve reports for", example = "1") @PathVariable Long campaign_id) {
        return ResponseEntity.ok(campaignService.getCampaignReports(campaign_id));
    }

    @Operation(summary = "Create a report for a campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CampaignReport.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{campaign_id}/reports")
    public ResponseEntity<CampaignReport> createReport(
            @Parameter(description = "ID of the campaign to create a report for", example = "1") @PathVariable Long campaign_id,
            @Parameter(description = "PDF file of the report") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Name of the report", example = "Q1 Report") @RequestParam("name") String name) {
        try {
            CampaignReport report = campaignService.createCampaignReport(campaign_id, name, file.getBytes());
            return ResponseEntity.ok(report);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Download a campaign report by report ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report downloaded successfully", content = {
            @Content(
                mediaType = "application/pdf",
                schema = @Schema(type = "string", format = "binary")
            )
        }),
        @ApiResponse(responseCode = "404", description = "Report not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/reports/{report_id}/download")
    public ResponseEntity<byte[]> downloadReport(@Parameter(description = "ID of the report to download", example = "1") @PathVariable Long report_id) {
        CampaignReport report = campaignService.getReportById(report_id);

        if (report.getPdfData() == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report.getPdfData());
    }
}