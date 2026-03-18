package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.EmailTemplate;
import com.operimus.Marketing.services.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.operimus.Marketing.services.MarketingIndexerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/${api.version}/email-templates") 
@Tag(name = "Email Templates", description = "Endpoints for managing email templates")
public class EmailTemplateController {
    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;


    @Operation(summary = "Create a new email template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Email template created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EmailTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
	@PostMapping
	public ResponseEntity<EmailTemplate> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email template to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = EmailTemplate.class)
            )
        )
        @RequestBody EmailTemplate template) {
		EmailTemplate saved = emailTemplateService.createTemplate(template);
        marketingIndexerService.indexEmailTemplate(saved);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

    @Operation(summary = "Get all email templates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of email templates", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EmailTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<java.util.List<EmailTemplate>> getAll() {
        return ResponseEntity.ok(emailTemplateService.getAllTemplates());
    }

    @Operation(summary = "Get an email template by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the email template", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EmailTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Email template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplate> getById(@Parameter(description = "ID of the email template to retrieve", example = "1") @PathVariable Long id) {
        return emailTemplateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update an existing email template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email template updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EmailTemplate.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Email template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplate> update(
        @Parameter(description = "ID of the email template to update", example = "1") @PathVariable Long id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated email template data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = EmailTemplate.class)
            )
        )
        @RequestBody EmailTemplate updated) {
        try {
            EmailTemplate saved = emailTemplateService.updateTemplate(id, updated);
            marketingIndexerService.indexEmailTemplate(saved);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete an email template by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Email template deleted successfully", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID of the email template to delete", example = "1") @PathVariable Long id) {
        emailTemplateService.deleteTemplate(id);
        marketingIndexerService.deleteFromIndex(id, "EMAIL_TEMPLATE");
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @Operation(summary = "Test an email template by sending test emails to all leads")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test emails sent successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Email template not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{id}/test")
    public ResponseEntity<Void> testEmailTemplate(
        @Parameter(description = "ID of the email template to test", example = "1") @PathVariable Long id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Request body containing test email details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = java.util.Map.class)
            )
        ) @RequestBody java.util.Map<String, String> request) {
        try {
            String testEmail = request.get("testEmail");
            if (testEmail == null || testEmail.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            emailTemplateService.test_emailTemplate(id, testEmail);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Template not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}