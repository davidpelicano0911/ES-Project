package com.operimus.Marketing.controllers;

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
import org.springframework.web.bind.annotation.RestController;

import com.operimus.Marketing.entities.WorkflowTemplate;
import com.operimus.Marketing.services.WorkflowTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.operimus.Marketing.services.MarketingIndexerService;

@RestController
@RequestMapping("/api/${api.version}/workflow-templates")
@Tag(name = "Workflow Templates", description = "Endpoints for managing workflow templates")
public class WorkflowTemplateController {
    @Autowired
    private WorkflowTemplateService workflowTemplateService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;

    @Operation(summary = "Create a new workflow template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Workflow template created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkflowTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<WorkflowTemplate> createWorkflowTemplate(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Workflow template to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = WorkflowTemplate.class)
            )
        )
        @RequestBody WorkflowTemplate template) {
        WorkflowTemplate newTemplate = workflowTemplateService.createWorkflowTemplate(template);
        marketingIndexerService.indexWorkflowTemplate(newTemplate);
        return ResponseEntity.ok(newTemplate);
    }

    @Operation(summary = "Get all workflow templates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of workflow templates", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkflowTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<WorkflowTemplate>> getAllWorkflowTemplates() {
        List<WorkflowTemplate> templates = workflowTemplateService.getAllWorkflowTemplates();
        return ResponseEntity.ok(templates);
    }

    @Operation(summary = "Get a workflow template by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the workflow template", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkflowTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow template not found", content = @Content)
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<WorkflowTemplate> getWorkflowTemplateById(@Parameter(description = "ID of the workflow template to retrieve", example = "1") @PathVariable Long templateId) {
        WorkflowTemplate template = workflowTemplateService.getWorkflowTemplateById(templateId);
        return ResponseEntity.ok(template);
    }

    @Operation(summary = "Update a workflow template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Workflow template updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkflowTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "404", description = "Workflow template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{templateId}")
    public ResponseEntity<WorkflowTemplate> updateWorkflowTemplate(
        @Parameter(description = "ID of the workflow template to update", example = "1") @PathVariable Long templateId, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated workflow template data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = WorkflowTemplate.class)
            )
        )
        @RequestBody WorkflowTemplate updatedTemplate) {
        WorkflowTemplate template = workflowTemplateService.updateWorkflowTemplate(templateId, updatedTemplate);
        marketingIndexerService.indexWorkflowTemplate(template);
        return ResponseEntity.ok(template);
    }

    @Operation(summary = "Delete a workflow template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Workflow template deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Workflow template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteWorkflowTemplate(@Parameter(description = "ID of the workflow template to delete", example = "1") @PathVariable Long templateId) {
        workflowTemplateService.deleteWorkflowTemplate(templateId);
        marketingIndexerService.deleteFromIndex(templateId, "WORKFLOW_TEMPLATE");
        return ResponseEntity.noContent().build();
    }
}
