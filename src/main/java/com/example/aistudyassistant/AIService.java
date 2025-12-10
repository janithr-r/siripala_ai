package com.example.aistudyassistant;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final ChatLanguageModel chatModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    // 1. Chat Memory (Anthima message 10 mathaka thiya gannawa)
    private final ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

    public AIService(@Value("${google.ai.gemini.api-key}") String apiKey,
                     EmbeddingStore<TextSegment> embeddingStore,
                     EmbeddingModel embeddingModel) {

        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;

        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash")
                .temperature(0.3)
                .build();
    }

    public String getAnswer(String userQuestion) {
        try {
            // --- RAG SEARCH PART ---
            var questionEmbedding = embeddingModel.embed(userQuestion).content();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(questionEmbedding)
                            .maxResults(3)
                            .minScore(0.5)
                            .build()
            );

            // 2. Citations Logic (Pitu anka hoyana kotasa)
            String relevantInformation = searchResult.matches().stream()
                    .map(match -> {
                        String text = match.embedded().text();
                        // Metadata walin file name eka hari page number eka hari gannawa
                        String source = match.embedded().metadata().getString("file_name");
                        return text + "\n[Source: " + (source != null ? source : "PDF Context") + "]";
                    })
                    .collect(Collectors.joining("\n\n"));

            // --- MEMORY & PROMPT PART ---

            // 3. User ge prashne Memory ekata danawa (Clean version eka)
            chatMemory.add(UserMessage.from(userQuestion));

            List<ChatMessage> messagesToSend = new ArrayList<>();

            // 4. Case 1: PDF Data hambune nattam -> Memory eka witharak yawanna
            if (relevantInformation.isEmpty()) {
                messagesToSend.addAll(chatMemory.messages());
            }
            // 5. Case 2: PDF Data thiyenawa -> Memory + Context dekama yawanna
            else {
                // System Message ekak widiyata Context eka yawanawa (Me weleta witharai)
                String systemPrompt = "You are a study assistant. Answer based ONLY on the following context:\n" +
                        relevantInformation;

                messagesToSend.add(SystemMessage.from(systemPrompt));
                messagesToSend.addAll(chatMemory.messages()); // Parana katha bahath ekathu karanawa
            }

            // 6. Gemini ta yawala uththare gannawa
            var response = chatModel.generate(messagesToSend);

            // 7. AI ge uththare Memory ekata save karanawa
            chatMemory.add(response.content());

            return response.content().text();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}