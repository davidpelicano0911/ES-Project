package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.LandingPageEvent;
import com.operimus.Marketing.services.LandingPageEventService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/landing-page-events")
@Tag(name = "Landing Page Events", description = "Endpoints for managing landing page events")
public class LandingPageEventController {

    @Autowired
    private LandingPageEventService landingPageEventService;

    @Operation(summary = "Get landing page events by lead ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of landing page events", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LandingPageEvent.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<LandingPageEvent>> getByLeadId(@Parameter(description = "ID of the lead to retrieve landing page events for", example = "1") @RequestParam(required = true) Long leadId) {
        List<LandingPageEvent> events = landingPageEventService.getByLeadId(leadId);
        return ResponseEntity.ok(events);
    }
}
