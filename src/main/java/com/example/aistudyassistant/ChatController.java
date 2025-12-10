package com.example.aistudyassistant;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AIService aiService;
    private final PdfService pdfService; // Aluth service eka

    public ChatController(AIService aiService, PdfService pdfService) {
        this.aiService = aiService;
        this.pdfService = pdfService;
    }

    // 1. PDF Upload karana endpoint eka
    @PostMapping("/upload")
    public String uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            return pdfService.uploadPdf(file.getInputStream());
        } catch (IOException e) {
            return "Upload karanna bari una: " + e.getMessage();
        }
    }

    // 2. Chat karana endpoint eka
    @GetMapping
    public String chat(@RequestParam String message) {
        return aiService.getAnswer(message);
    }
}