package com.operimus.Marketing.controllers;

import com.operimus.Marketing.dto.LeadSyncStatusDTO;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.services.LeadService;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.operimus.Marketing.services.MarketingIndexerService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/leads")
@Tag(name = "Leads", description = "Endpoints for managing leads")
public class LeadController {

    @Autowired
    private LeadService leadService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;

    @Operation(summary = "Create a new lead")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Lead created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Lead.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Lead> createLead(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Lead to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = Lead.class)
            )
        )
        @RequestBody Lead lead) {
        Lead createdLead = leadService.createLead(lead);
        
        return new ResponseEntity<>(createdLead, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all leads")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of leads", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Lead.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Lead>> getAllLeads() {
        List<Lead> leads = leadService.getAllLeads();
        return ResponseEntity.ok(leads);
    }

    @Operation(summary = "Get a lead by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the lead", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Lead.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Lead not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Lead> getLead(@Parameter(description = "ID of the lead to retrieve", example = "1") @PathVariable Long id) {
        Lead lead = leadService.getLead(id);
        if (lead != null) {
            return ResponseEntity.ok(lead);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get and compute lead score by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the lead score", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Integer.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Lead not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}/score")
    public ResponseEntity<Integer> getAndComputeScore(@Parameter(description = "ID of the lead to retrieve the score for", example = "1") @PathVariable Long id) {
        // Get the lead and return its score (auto-computed by JPA hooks)
        Lead lead = leadService.getLead(id);
        return ResponseEntity.ok(lead.getScore() != null ? lead.getScore() : 0);
    }

    @Operation(summary = "Update a lead by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lead updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Lead.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Lead not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Lead> updateLead(
        @Parameter(description = "ID of the lead to update", example = "1") @PathVariable Long id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated lead data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = Lead.class)
            )
        ) @RequestBody Lead lead) {
        Lead updatedLead = leadService.updateLead(id, lead);
        if (updatedLead != null) {
            marketingIndexerService.indexLead(updatedLead);
            return ResponseEntity.ok(updatedLead);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Delete a lead by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Lead deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Lead not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@Parameter(description = "ID of the lead to delete", example = "1") @PathVariable Long id) {
        boolean deleted = leadService.deleteLead(id);
        if (deleted) {
            marketingIndexerService.deleteFromIndex(id, "LEAD");
            return ResponseEntity.noContent().build(); 
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get lead ID by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the lead ID", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Long.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/id-by-email")
    public ResponseEntity<Long> getLeadIdByEmail(@Parameter(description = "Email of the lead to retrieve the ID for", example = "example@example.com") @RequestParam String email) {
        Long id = leadService.getLeadIdByEmail(email);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Get lead ID by phone number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the lead ID", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Long.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/id-by-phone")
    public ResponseEntity<Long> getLeadIdByPhone(@Parameter(description = "Phone number of the lead to retrieve the ID for", example = "911234567") @RequestParam String phoneNumber) {
        Long id = leadService.getLeadIdByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Sync all leads from HubSpot")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful synchronization of leads", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/sync-from-hubspot")
    public ResponseEntity<Map<String, Object>> syncAllLeads() {
        Map<String, Object> result = leadService.syncAllFromHubSpot();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get lead synchronization status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of lead sync status", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LeadSyncStatusDTO.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/sync-status")
    public ResponseEntity<LeadSyncStatusDTO> getSyncStatus() {
        return ResponseEntity.ok(leadService.getSyncStatus());
    }

}
