package com.ieltsgrading.ielts_evaluator.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ✅ Controller to handle audio file uploads from speaking practice
 */
@RestController
@RequestMapping("/api")
public class AudioUploadController {

    // Upload directory - configurable via application.properties
    @Value("${audio.upload.dir:uploads/audio}")
    private String uploadDir;

    /**
     * Upload audio file from speaking practice
     * 
     * POST /api/upload/audio
     * 
     * @param file - MultipartFile audio file (webm, mp3, wav, etc.)
     * @return JSON with file path/URL
     */
    @PostMapping("/audio")
    public ResponseEntity<Map<String, Object>> uploadAudio(
            @RequestParam("audioFile") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("error", "File too large. Maximum 10MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create upload directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create upload directory");
                }
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file to disk
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());
            
            // Get absolute path for API usage
            String absolutePath = filePath.toAbsolutePath().toString();
            
            // Return success response
            response.put("success", true);
            response.put("filename", filename);
            response.put("filePath", absolutePath); // Full path for API
            response.put("fileUrl", "/uploads/audio/" + filename); // URL for playback
            response.put("fileSize", file.getSize());
            
            System.out.println("✅ Audio uploaded successfully:");
            System.out.println("   Filename: " + filename);
            System.out.println("   Path: " + absolutePath);
            System.out.println("   Size: " + file.getSize() + " bytes");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            System.err.println("❌ Upload error: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get file info (optional endpoint)
     */
    @GetMapping("/audio/{filename}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String filename) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path filePath = Paths.get(uploadDir, filename);
            File file = filePath.toFile();
            
            if (!file.exists()) {
                response.put("error", "File not found");
                return ResponseEntity.notFound().build();
            }
            
            response.put("filename", filename);
            response.put("filePath", filePath.toAbsolutePath().toString());
            response.put("fileSize", file.length());
            response.put("exists", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}