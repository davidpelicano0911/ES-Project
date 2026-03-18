package com.operimus.Marketing.controllers;

import java.util.Map;
import com.operimus.Marketing.entities.FormSubmission;
import com.operimus.Marketing.services.FormSubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/form-submissions")
@Tag(name = "Form Submissions", description = "Endpoints for managing form submissions")
public class FormSubmissionController {

    @Autowired
    private FormSubmissionService submissionService;


    @Operation(summary = "Submit a new form submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Form submission created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormSubmission.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<FormSubmission> submit(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Form submission data", 
            required = true,
            content = @Content(
                schema = @Schema(implementation = Map.class)
            )
        ) @RequestBody Map<String, Object> body) {
        try {
            System.out.println("Authenticated form submission received: " + body);
            FormSubmission saved = submissionService.createSubmission(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            System.err.println("Error in authenticated form submission: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Get form submissions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of form submissions", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormSubmission.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<FormSubmission>> list(@Parameter(description = "Filter by form ID", example = "1") @RequestParam(required = false) Long formId,
                                                      @Parameter(description = "Filter by campaign ID", example = "1") @RequestParam(required = false) Long campaignId,
                                                      @Parameter(description = "Filter by lead ID", example = "1") @RequestParam(required = false) Long leadId) {
        try {
            List<FormSubmission> submissions = submissionService.getSubmissions(formId, campaignId, leadId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Submit a new public form submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Form submission created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormSubmission.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/public")
    public ResponseEntity<FormSubmission> submitPublic(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Public form submission data", 
            required = true,
            content = @Content(
                schema = @Schema(implementation = Map.class)
            )
        ) @RequestBody Map<String, Object> body) {
        try {
            System.out.println("Public form submission received: " + body);
            FormSubmission saved = submissionService.createSubmission(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            System.err.println("Error in public form submission: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Get form performance statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of form performance statistics", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getStats() {
        try {
            List<Map<String, Object>> stats = submissionService.getFormPerformanceStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
