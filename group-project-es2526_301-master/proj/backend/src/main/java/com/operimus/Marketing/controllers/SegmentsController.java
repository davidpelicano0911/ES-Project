package com.operimus.Marketing.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.services.SegmentsService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.operimus.Marketing.services.MarketingIndexerService;

@RestController
@RequestMapping("/api/${api.version}/segments")
@Tag(name = "Segments", description = "Endpoints for managing segments")
public class SegmentsController {
    @Autowired
    private SegmentsService segmentsService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;

    @Operation(summary = "Get all segments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of segments", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Segments.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/")
    public List<Segments> getAllSegments() {
        return segmentsService.getAllSegments();
    }

    @Operation(summary = "Create a new segment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Segment created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Segments.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<Segments> createSegment(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Segment to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = Segments.class)
            )
        ) @RequestBody Segments segment) {
        try {
            Segments createdSegment = segmentsService.createSegment(segment);
            marketingIndexerService.indexSegment(createdSegment);
            return ResponseEntity.ok(createdSegment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
