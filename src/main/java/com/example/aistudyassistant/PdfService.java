package com.example.aistudyassistant;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class PdfService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public PdfService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public String uploadPdf(InputStream pdfStream) {
        try {
            // 1. InputStream eka Temp File ekakata save karagannawa (Page numbers ganna lesi wenna)
            Path tempFile = Files.createTempFile("upload", ".pdf");
            Files.copy(pdfStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 2. PDF eka load karanawa (Parsing)
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = FileSystemDocumentLoader.loadDocument(tempFile, parser);

            // 3. Ingestor eka configure karanawa
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    // MEKA ALUTH: Pituwen pituwata kadanawa (Metadata ekka)
                    .documentSplitter(DocumentSplitters.recursive(1000, 200))
                    .build();

            ingestor.ingest(document);

            // Temp file eka makala danawa
            Files.delete(tempFile);

            return "PDF processed successfully! (With Citations support)";

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}