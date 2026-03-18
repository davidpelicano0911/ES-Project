package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.FormTemplate;
import com.operimus.Marketing.dto.PublicFormDTO;
import com.operimus.Marketing.services.FormTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.operimus.Marketing.services.MarketingIndexerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/form-template")
@Tag(name = "Form Templates", description = "Endpoints for managing form templates")
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;

    @Autowired
    public FormTemplateController(FormTemplateService formTemplateService) {
        this.formTemplateService = formTemplateService;
    }

    @Operation(summary = "Create a new form template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Form template created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("")
    public ResponseEntity<FormTemplate> createFormTemplate(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Form template to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = FormTemplate.class)
            )
        )
        @RequestBody FormTemplate request) {
        FormTemplate response = formTemplateService.createFormTemplate(request);
        marketingIndexerService.indexFormTemplate(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all form templates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of form templates", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<List<FormTemplate>> getAllFormTemplates() {
        List<FormTemplate> templates = formTemplateService.getAllFormTemplates();
        return ResponseEntity.ok(templates);
    }

    @Operation(summary = "Get a form template by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the form template", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Form template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<FormTemplate> getFormTemplate(@Parameter(description = "ID of the form template to retrieve", example = "1") @PathVariable Long id) {
        FormTemplate response = formTemplateService.getFormTemplate(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a form template by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Form template updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Form template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<FormTemplate> updateFormTemplate(
        @Parameter(description = "ID of the form template to update", example = "1") @PathVariable Long id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Form template to update", 
            required = true, 
            content = @Content(
                schema = @Schema(implementation = FormTemplate.class)
            )
        ) @RequestBody FormTemplate request) {
        FormTemplate response = formTemplateService.updateFormTemplate(id, request);
        marketingIndexerService.indexFormTemplate(response);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a form template by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Form template deleted successfully", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormTemplate(@Parameter(description = "ID of the form template to delete", example = "1") @PathVariable Long id) {
        formTemplateService.deleteFormTemplate(id);
        marketingIndexerService.deleteFromIndex(id, "FORM_TEMPLATE");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a public form template by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the public form template", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PublicFormDTO.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Form template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/public/{id}")
    public ResponseEntity<PublicFormDTO> getPublicFormTemplate(@Parameter(description = "ID of the public form template to retrieve", example = "1") @PathVariable Long id) {
        FormTemplate formTemplate = formTemplateService.getFormTemplate(id);
        if (formTemplate == null) {
            return ResponseEntity.notFound().build();
        }
        PublicFormDTO publicFormDTO = new PublicFormDTO(formTemplate.getId(), formTemplate.getFormJson());
        return ResponseEntity.ok(publicFormDTO);
    }
}
