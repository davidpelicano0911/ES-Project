package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.CampaignMaterials;
import com.operimus.Marketing.services.CampaignService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/materials")
@Tag(name = "Materials", description = "Endpoints for managing marketing materials")
public class MaterialController {

    @Autowired
    private CampaignService campaignService;

    @Operation(summary = "Get all campaign materials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of campaign materials", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CampaignMaterials.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public List<CampaignMaterials> getAllMaterials() {
        return campaignService.getAllCampaignMaterials();
    }
}
