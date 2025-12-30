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

    // time eka
    @GetMapping("/time")
    public String checkTime() {
        return "<h1>Server Time Check 🕒</h1>" +
                "<p>Current Time: " + java.time.LocalDateTime.now() + "</p>";
    }

    // 4. System Info check karana endpoint eka
    @GetMapping("/info")
    public String getSystemInfo() {
        return "<h2>System Info 💻</h2>" +
                "<p><strong>OS:</strong> " + System.getProperty("os.name") + "</p>" +
                "<p><strong>Java Version:</strong> " + System.getProperty("java.version") + "</p>" +
                "<p><strong>User:</strong> " + System.getProperty("user.name") + "</p>";
    }
}