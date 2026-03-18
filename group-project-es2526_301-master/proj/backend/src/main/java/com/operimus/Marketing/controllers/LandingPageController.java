package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.LandingPage;
import com.operimus.Marketing.dto.PublicLandingPageDTO;
import com.operimus.Marketing.services.LandingPageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/${api.version}/landing-pages")
@Tag(name = "Landing Pages", description = "Endpoints for managing landing pages")
public class LandingPageController {

    @Autowired
    private LandingPageService landingPageService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;

    @Operation(summary = "Create a new landing page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Landing page created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LandingPage.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<LandingPage> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Landing page to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LandingPage.class)
            )
        )
        @RequestBody LandingPage page) {
        LandingPage saved = landingPageService.createPage(page);
        marketingIndexerService.indexLandingPage(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Get all landing pages")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of landing pages", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LandingPage.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<LandingPage>> getAllPages() {
        List<LandingPage> pages = landingPageService.getAllPages();
        return ResponseEntity.ok(pages);
    }

    @Operation(summary = "Get a landing page by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the landing page", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LandingPage.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Landing page not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<LandingPage> getPageById(@Parameter(description = "ID of the landing page to retrieve", example = "1") @PathVariable Long id) {
        return landingPageService.getPageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a landing page by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Landing page updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LandingPage.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Landing page not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<LandingPage> updatePage(
        @Parameter(description = "ID of the landing page to update", example = "1") @PathVariable Long id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated landing page data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LandingPage.class)
            )
        )
        @RequestBody LandingPage updated) {
        try {
            LandingPage updatedPage = landingPageService.updatePage(id, updated);
            marketingIndexerService.indexLandingPage(updatedPage);
            return ResponseEntity.ok(updatedPage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a landing page by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Landing page deleted successfully", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@Parameter(description = "ID of the landing page to delete", example = "1") @PathVariable Long id) {
        landingPageService.deletePage(id);
        marketingIndexerService.deleteFromIndex(id, "LANDING_PAGE");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a public landing page by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the public landing page", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PublicLandingPageDTO.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Landing page not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/public/{id}")
    public ResponseEntity<PublicLandingPageDTO> getPublicPageById(@Parameter(description = "ID of the public landing page to retrieve", example = "1") @PathVariable Long id) {
        return landingPageService.getPageById(id)
                .map(page -> {
                    PublicLandingPageDTO dto = new PublicLandingPageDTO(page.getId(), page.getBody(), page.getDesign());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
