package com.operimus.Marketing.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.operimus.Marketing.services.WorkflowEngine;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.operimus.Marketing.services.LandingPageEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.operimus.Marketing.dto.EventDTO;

@RestController
@RequestMapping("/api/${api.version}/events")
@Tag(name = "Events", description = "Endpoints for managing events")
public class EventController {
    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private LandingPageEventService landingPageEventService;

    @Operation(summary = "Create a new event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event processed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public void createEvent(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Event data to process",
            required = true,
            content = {
                @Content(
                    schema = @Schema(implementation = EventDTO.class)
                )
            }
        )
        @RequestBody EventDTO eventDTO) {
        // Persist landing page related events when possible
        try {
            landingPageEventService.saveEvent(eventDTO);
        } catch (Exception ex) {
            // ignore persistence errors
        }

        // Forward to workflow engine for rule handling
        workflowEngine.handleEvent(eventDTO);
    }
}
