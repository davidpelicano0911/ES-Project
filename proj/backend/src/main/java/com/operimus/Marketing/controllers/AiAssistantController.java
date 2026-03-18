package com.operimus.Marketing.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.operimus.Marketing.services.MarketingRagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@RestController
@RequestMapping("/api/${api.version}/ai")
@Tag(name = "AI Assistant", description = "Endpoints for marketing assistance")
public class AiAssistantController {

    private final MarketingRagService ragService;

    public AiAssistantController(MarketingRagService ragService) {
        this.ragService = ragService;
    }

    @Operation(summary = "Ask a question to the AI assistant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful response with AI answer", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AiAnswerDto.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody AiQuestionDto request) {
        try {
            String chatId = (request.getConversationId() != null && !request.getConversationId().isEmpty()) 
                ? request.getConversationId() 
                : UUID.randomUUID().toString();

            System.out.println("💬 Chat ID: " + chatId + " | Q: " + request.getQuestion());

            String answer = ragService.ask(request.getQuestion(), chatId);
            
            return ResponseEntity.ok(new AiAnswerDto(answer, chatId));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Server Error: " + e.getMessage());
        }
    }

    public static class AiQuestionDto {
        private String question;
        private String conversationId; 

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    }

    public static class AiAnswerDto {
        private String answer;
        private String conversationId;

        public AiAnswerDto(String answer, String conversationId) { 
            this.answer = answer; 
            this.conversationId = conversationId;
        }
        
        public String getAnswer() { return answer; }
        public String getConversationId() { return conversationId; }
    }
}