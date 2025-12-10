package com.example.aistudyassistant; // <-- Menna meka wenas kala

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
// Me line eka hariyatama thiyenawada balanna:
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;

@Configuration
public class AIConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            @Value("${pinecone.api-key}") String apiKey,
            @Value("${pinecone.index}") String indexName) {

        return PineconeEmbeddingStore.builder()
                .apiKey(apiKey)
                .index(indexName)
                .build();
    }
}