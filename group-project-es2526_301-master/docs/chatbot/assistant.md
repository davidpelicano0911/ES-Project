# Technical Documentation: AI Agent & Chatbot

This document details the implementation of the project's AI Agent (RAG with Agentic capabilities), describing the architecture, data flow, and component locations within the repository.

## 1. Architecture Overview

The system utilizes a **RAG (Retrieval-Augmented Generation)** approach enhanced with agentic capabilities. A React frontend communicates with a Spring Boot backend, which orchestrates vector document retrieval (via PostgreSQL/pgvector), interaction with the LLM (Gemini 2.0 Flash), and tool execution (function calling).

## 2. Frontend Implementation

The client consists of an embedded chat component that manages conversation state and API communication.

### UI Component
*   **File:** [`MyChat`](proj/frontend/src/components/MyChat.tsx)
*   **Path:** `proj/frontend/src/components/MyChat.tsx`
*   **Function:** Responsible for rendering messages, capturing user input, and providing visual loading feedback.

### Integration (Hook/API)
*   **File:** [`useChatApi`](proj/frontend/src/api/apiChat.tsx)
*   **Path:** `proj/frontend/src/api/apiChat.tsx`
*   **Function:**
    *   Manages the lifecycle of the `POST` request to `/ai/ask`.
    *   Constructs the payload: `{ question: string, conversationId?: string }`.
    *   Receives the response and updates the local state with `{ answer, conversationId }`.

## 3. Backend Implementation

The backend exposes REST endpoints and contains all business logic for indexing, vector search, and tool execution.

### REST Controller
*   **Class:** [`AiAssistantController`](proj/backend/src/main/java/com/operimus/Marketing/controllers/AiAssistantController.java)
*   **Path:** `proj/backend/src/main/java/com/operimus/Marketing/controllers/AiAssistantController.java`
*   **Endpoint:** `POST /api/${api.version}/ai/ask`
*   **Function:** API entry point. Ensures a `conversationId` exists and delegates the prompt to the Agent/RAG service.

### Agent Core (Service Layer)
*   **Orchestration:** [`MarketingRagService`](proj/backend/src/main/java/com/operimus/Marketing/services/MarketingRagService.java)
    *   **Path:** `proj/backend/src/main/java/com/operimus/Marketing/services/MarketingRagService.java`
    *   Executes `vectorStore.similaritySearch` to find relevant context.
    *   Composes the prompt (`CONTEXT + QUESTION`) and invokes the `ChatClient`.
    *   Uses `chat_memory_conversation_id` as an advisor parameter to maintain conversation history.
    *   The LLM is instructed to understand the query intent, classifying it into generic or specific. This is done to prevent the LLM to load the full indexer every single time, which can become heavy and complex.

*   **Indexing:** [`MarketingIndexerService`](proj/backend/src/main/java/com/operimus/Marketing/services/MarketingIndexerService.java)
    *   **Path:** `proj/backend/src/main/java/com/operimus/Marketing/services/MarketingIndexerService.java`
    *   Responsible for transforming raw data into vectors and storing them in the database.
    *   When the system is initialized the `IndexInitializer` is executed to load the initial database into the indexer, in order to provide the full context to the LLM.

*   **Tools:** [`MarketingTools`](proj/backend/src/main/java/com/operimus/Marketing/tool/MarketingTools.java)
    *   **Path:** `proj/backend/src/main/java/com/operimus/Marketing/tool/MarketingTools.java`
    *   Utilities and *function calling* methods used by the agent to perform system actions (e.g., creating posts, editing templates, generating pages).

## 4. Data Infrastructure & Configuration

### Database
*   **SQL Script:** [`data.sql`](proj/backend/src/main/resources/data.sql)
*   **Structure:**
    *   `documents` table: Stores vectors (supports `vector(768)`).
    *   `spring_ai_chat_memory` table: Stores conversation history.

### Configuration
*   **Production:** [`application.properties`](proj/backend/src/main/resources/application.properties)
    *   Spring AI parameters, `pgvector` settings, and API keys (`GEMINI_API_KEY`).
*   **Testing:** [`application-test.yml`](proj/backend/src/test/resources/application-test.yml)
    *   Isolated definitions for the integration test environment.

## 5. Execution Flow

1.  **User:** Types and sends a question or command via the UI ([`MyChat`](proj/frontend/src/components/MyChat.tsx)).
2.  **Frontend:** The [`useChatApi`](proj/frontend/src/api/apiChat.tsx) hook sends a `POST` request with the question and conversation ID (if available).
3.  **Controller:** [`AiAssistantController`](proj/backend/src/main/java/com/operimus/Marketing/controllers/AiAssistantController.java) receives the request.
4.  **Agent Service:**
    *   Performs a similarity search in the Database (`VectorStore`).
    *   Concatenates retrieved documents to the prompt.
    *   Sends the enriched prompt to the LLM (Gemini).
    *   **Decision:** The LLM decides whether to respond directly or invoke a tool (e.g., create a landing page) defined in `MarketingTools`.
5.  **Response:** The generated text and `conversationId` are returned to the Frontend for display.

## 6. Quick File Reference

| Component | Function | Path |
| :--- | :--- | :--- |
| **Frontend UI** | Chat Interface | [`MyChat.tsx`](proj/frontend/src/components/MyChat.tsx) |
| **Frontend API** | Communication Hook | [`apiChat.tsx`](proj/frontend/src/api/apiChat.tsx) |
| **Controller** | HTTP Endpoint | [`AiAssistantController.java`](proj/backend/src/main/java/com/operimus/Marketing/controllers/AiAssistantController.java) |
| **Service** | Agent/RAG Logic | [`MarketingRagService.java`](proj/backend/src/main/java/com/operimus/Marketing/services/MarketingRagService.java) |
| **Indexer** | Vector Indexing | [`MarketingIndexerService.java`](proj/backend/src/main/java/com/operimus/Marketing/services/MarketingIndexerService.java) |
| **Tools** | Agent Tools | [`MarketingTools.java`](proj/backend/src/main/java/com/operimus/Marketing/tool/MarketingTools.java) |
| **DB Script** | SQL Schema | [`data.sql`](proj/backend/src/main/resources/data.sql) |
| **Index Initializer** | Init Indexer | [`IndexInitializer.java`](proj/backend/src/main/java/com/operimus/Marketing/init/IndexInitializer.java) |