package com.operimus.Marketing.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.services.WorkflowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Set;

import com.operimus.Marketing.dto.WorkflowDTO;
import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.services.MarketingIndexerService;


@RestController
@RequestMapping("/api/${api.version}/workflows")
@Tag(name = "Workflows", description = "Endpoints for managing workflows")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;


    @Operation(summary = "Create a new workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Workflow created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Workflow.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Workflow to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = WorkflowDTO.class)
            )
        )
        @RequestBody WorkflowDTO request) {
        Workflow workflow = workflowService.createWorkflow(request);
        marketingIndexerService.indexWorkflow(workflow);
        return ResponseEntity.ok(workflow);
    }


    @Operation(summary = "Get all workflows")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of workflows", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Workflow.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }

    @Operation(summary = "Get a workflow by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the workflow", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Workflow.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{workflowId}")
    public ResponseEntity<Workflow> getWorkflow(@Parameter(description = "ID of the workflow to retrieve", example = "1") @PathVariable Long workflowId) {
        try {
            Workflow workflow = workflowService.getWorkflowWithNodes(workflowId);
            return ResponseEntity.ok(workflow);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a workflow by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Workflow updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Workflow.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{workflowId}")
    public ResponseEntity<Workflow> updateWorkflow(
        @Parameter(description = "ID of the workflow to update", example = "1") @PathVariable Long workflowId, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated workflow data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = Workflow.class)
            )
        )
        @RequestBody Workflow updatedWorkflow) {
        try {
            Workflow workflow = workflowService.updateWorkflow(workflowId, updatedWorkflow);
            marketingIndexerService.indexWorkflow(workflow);
            return ResponseEntity.ok(workflow);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a workflow by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Workflow deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Workflow not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{workflowId}")
    public ResponseEntity<Void> deleteWorkflow(@Parameter(description = "ID of the workflow to delete", example = "1") @PathVariable Long workflowId) {
        try {
            workflowService.deleteWorkflow(workflowId);
            marketingIndexerService.deleteFromIndex(workflowId, "WORKFLOW");
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create a workflow from a template for a specific campaign")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Workflow created successfully from template", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Workflow.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/campaigns/{campaignId}/from-template/{templateId}")
    public ResponseEntity<Workflow> createWorkflowFromTemplate(
            @Parameter(description = "ID of the campaign", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "ID of the template", example = "1") @PathVariable Long templateId) {
        Workflow workflow = workflowService.createWorkflowFromTemplate(templateId, campaignId);
        marketingIndexerService.indexWorkflow(workflow);
        return ResponseEntity.ok(workflow);
    }


    //////////////////////////////////////////
    /////////////////////////////////////////

    @Operation(summary = "Get all nodes for a workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of nodes", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Node.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{workflowId}/nodes")
    public ResponseEntity<List<Node>> getNodes(@Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId) {
        try {
            List<Node> nodes = workflowService.getNodes(workflowId);
            return ResponseEntity.ok(nodes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Add a node to a workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Node added successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Node.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{workflowId}/nodes")
    public ResponseEntity<Node> addNode(
        @Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Node to add",
            required = true,
            content = @Content(
                schema = @Schema(implementation = Node.class)
            )
        ) @RequestBody Node node) {
        try {
            Node createdNode = workflowService.addNode(workflowId, node);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNode);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get a node by ID for a specific workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the node", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Node.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow or node not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{workflowId}/nodes/{nodeId}")
    public ResponseEntity<Node> getNode(
        @Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId, 
        @Parameter(description = "ID of the node", example = "1") @PathVariable Long nodeId) {
        try {
            Node node = workflowService.getNode(workflowId, nodeId);
            return ResponseEntity.ok(node);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a node by ID from a specific workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Node deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Workflow or node not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{workflowId}/nodes/{nodeId}")
    public ResponseEntity<Void> deleteNode(
        @Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId, 
        @Parameter(description = "ID of the node", example = "1") @PathVariable Long nodeId) {
        try {
            workflowService.removeNode(workflowId, nodeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Clear all nodes from a specific workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "All nodes cleared successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Workflow not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{workflowId}/nodes/clear")
    public ResponseEntity<Void> clearNodes(@Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId) {
        try {
            workflowService.clearNodes(workflowId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a node by ID for a specific workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Node updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Node.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow or node not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{workflowId}/nodes/{nodeId}")
    public ResponseEntity<Node> updateNode(
        @Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId, 
        @Parameter(description = "ID of the node", example = "1") @PathVariable Long nodeId, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated node data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = Node.class)
            )    
        ) @RequestBody Node updatedNode) {
        try {
            Node existingNode = workflowService.getNode(workflowId, nodeId);
            existingNode.setNodeType(updatedNode.getNodeType());
            existingNode.setIsEndNode(updatedNode.getIsEndNode());
            existingNode.setIsStartNode(updatedNode.getIsStartNode());
            existingNode.setPositionX(updatedNode.getPositionX());
            existingNode.setPositionY(updatedNode.getPositionY());
            Node savedNode = workflowService.addNode(workflowId, existingNode);
            return ResponseEntity.ok(savedNode);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    


    ///////////////////////////////////////////
    ///////////////////////////////////////////

    @Operation(summary = "Add a connection (edge) between two nodes in a workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Connection added successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Workflow or nodes not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{workflowId}/nodes/{fromNodeId}/edges/{toNodeId}")
    public ResponseEntity<Void> addConnection(
        @Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId, 
        @Parameter(description = "ID of the source node", example = "1") @PathVariable Long fromNodeId, 
        @Parameter(description = "ID of the target node", example = "2") @PathVariable Long toNodeId) {
        try {
            workflowService.addConnection(workflowId, fromNodeId, toNodeId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get all connections (edges) from a specific node in a workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of connections", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Node.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Workflow or node not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{workflowId}/nodes/{fromNodeId}/edges/")
    public ResponseEntity<Set<Node>> getConnection(
        @Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId, 
        @Parameter(description = "ID of the source node", example = "1") @PathVariable Long fromNodeId) {
        try {
            Set<Node> connections = workflowService.getConnection(workflowId, fromNodeId);
            return ResponseEntity.ok(connections);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a connection (edge) between two nodes in a workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Connection deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Workflow or nodes not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{workflowId}/nodes/{fromNodeId}/edges/{toNodeId}")
    public ResponseEntity<Void> deleteConnection(
        @Parameter(description = "ID of the workflow", example = "1") @PathVariable Long workflowId, 
        @Parameter(description = "ID of the source node", example = "1") @PathVariable Long fromNodeId, 
        @Parameter(description = "ID of the target node", example = "2") @PathVariable Long toNodeId) {
        try {
            workflowService.removeConnection(workflowId, fromNodeId, toNodeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //////////////////////////////////////////
    /////////////////////////////////////////

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
