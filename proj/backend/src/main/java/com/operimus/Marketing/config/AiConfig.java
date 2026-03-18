// package com.operimus.marketing.config; // Adjust package as needed
// 
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.ai.embedding.EmbeddingModel;
// import org.springframework.ai.google.genai.GoogleGenAiApi; // Core API client
// import org.springframework.ai.google.genai.embedding.GoogleGenAiTextEmbeddingModel; // The specific EmbeddingModel implementation
// 
// @Configuration
// public class AiConfig {
// 
//     @Bean
//     public EmbeddingModel embeddingModel(@Value("${spring.ai.google.gemini.api-key}") String apiKey) {
//         
//         // 1. Create the core API client using your API key.
//         GoogleGenAiApi googleGenAiApi = new GoogleGenAiApi(apiKey);
//         
//         // 2. Create and return the EmbeddingModel implementation.
//         // It will use the default options and the configured API key.
//         return new GoogleGenAiTextEmbeddingModel(googleGenAiApi);
//     }
// }
