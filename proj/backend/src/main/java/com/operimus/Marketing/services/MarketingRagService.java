package com.operimus.Marketing.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import com.operimus.Marketing.tool.MarketingTools;
import reactor.core.scheduler.Schedulers;
import java.time.Duration;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.SearchRequest;

@Service
public class MarketingRagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();

    public MarketingRagService(VectorStore vectorStore, 
                               ChatClient.Builder builder, 
                               MarketingTools marketingTools,
                               ChatMemory chatMemory) { 
        this.vectorStore = vectorStore;
        
        this.chatClient = builder
            .defaultSystem("""
                You are an autonomous Marketing Expert Agent.
            
            OPERATIONAL PROTOCOLS:
            
            1. FACTUAL QUESTIONS (RAG)
            - Use the provided CONTEXT to answer questions about the company.
            - When questioned about a specific entity, don't mention the ID and the similarity score in the answer.
            - If you are asked about a set of entities (e.g., how many campaigns exist?), check all elements with the type equalling the entity type requested.

            2. ACTION REQUESTS (TOOL EXECUTION)
            
            CASE A: COMPLETE INPUTS
            - If the user provides all necessary details to run the tool, EXECUTE IMMEDIATELY.
            
            CASE B: MISSING INPUTS (INTERACTIVE GUIDANCE)
            - If the request is vague or missing parameters, **DO NOT** ask for everything at once.
            - **DO NOT** generate the content immediately without approval.

            CASE C: WHEN DELETING OR UPDATING ENTITIES
            - Always ask for final confirmation before executing delete or update operations.
            - **DO NOT** proceed with deletions or updates without explicit user confirmation.
            
            INSTEAD, FOLLOW THIS LOOP:
            1. Identify the **single most important** missing detail.
            2. Ask the user **only** about that detail.
            3. Wait for the answer.
            4. When you have a clear idea, ask for **final confirmation** to execute the tool.
            
            3. LANGUAGE
            - Always answer and generate content in Portuguese (Portugal).
                """)
            .defaultTools(marketingTools)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }

    private enum QueryIntent {
        LIST_ALL, 
        SPECIFIC
    }

    private QueryIntent classifyQuery(String question) {

        String system = """
            You are an intent classifier for a RAG system.
            Classify the USER QUESTION in EXACTLY one category:

            - LIST_ALL   → asks for counts, lists, summaries (e.g: List all campaigns).
            - SPECIFIC   → asks about one specific entity (e.g: Give me the details of a campaign X). 

            Return ONLY one of:
            LIST_ALL
            SPECIFIC
            """;

        String response = chatClient
            .prompt()
            .system(system)
            .user(question)
            .call()
            .content()
            .trim();

        return QueryIntent.valueOf(response);
    }

    // private String detectMetadataType(String q) {
    //     q = q.toLowerCase();

    //     if (q.contains("campanha") || q.contains("campaign")) return "CAMPAIGN";
    //     if (q.contains("email")) return "EMAIL_TEMPLATE";
    //     if (q.contains("landing")) return "LANDING_PAGE";
    //     if (q.contains("form")) return "FORM_TEMPLATE";
    //     if (q.contains("workflow")) return "WORKFLOW";
    //     if (q.contains("segment")) return "SEGMENT";
    //     if (q.contains("post")) return "POST";
    //     if (q.contains("dashboard")) return "DASHBOARD";
    //     if (q.contains("lead")) return "LEAD";

    //     return null;
    // }

    private SearchRequest buildRagRequest(String question) {

        QueryIntent intent = classifyQuery(question);
        System.out.println("\n----> Classified intent: " + intent);

        int topK =
            switch (intent) {
                case LIST_ALL -> 1000;
                case SPECIFIC -> 4;
            };

        SearchRequest req = SearchRequest.builder().query(question).topK(topK).build();
        return req;
    }

    public String ask(String question, String conversationId) {
        
        SearchRequest request = buildRagRequest(question);

        List<Document> docs = vectorStore.similaritySearch(request);
        String context = docs.stream()
            .map(Document::getFormattedContent)
            .collect(Collectors.joining("\n---\n"));

        System.out.println("RAG → " + docs.size()
            + " docs | topK=" + request.getTopK()
            + " | filter=" + request.getFilterExpression());

        System.out.println("RAG CONTEXT:\n" + context + "\n----");

        return chatClient.prompt()
            .user("CONTEXT:\n" + context + "\n\nQUESTION:\n" + question)
            

            .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
            
            .call()
            .content();
    }
}