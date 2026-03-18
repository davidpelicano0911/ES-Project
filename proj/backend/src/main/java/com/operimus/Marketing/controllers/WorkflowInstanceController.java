package com.operimus.Marketing.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.List;
import com.operimus.Marketing.entities.WorkflowInstance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.operimus.Marketing.services.WorkflowInstanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/${api.version}/workflow-instances")
@Tag(name = "Workflow Instances", description = "Endpoints for managing workflow instances")
public class WorkflowInstanceController {
    
    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Operation(summary = "Get all workflow instances")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of workflow instances", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkflowInstance.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<List<WorkflowInstance>> getAllWorkflowInstances() {
        List<WorkflowInstance> list = workflowInstanceService.getAllInstances();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get workflow instances by workflow ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of workflow instances by workflow ID", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkflowInstance.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<List<WorkflowInstance>> getWorkflowInstancesByWorkflow(@Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId) {
        List<WorkflowInstance> list = workflowInstanceService.getInstancesByWorkflowId(workflowId);
        return ResponseEntity.ok(list);
    }

}
