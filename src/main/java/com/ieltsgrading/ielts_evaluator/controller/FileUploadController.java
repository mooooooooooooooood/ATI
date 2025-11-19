package com.ieltsgrading.ielts_evaluator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
public class FileUploadController {

    private static final Path UPLOAD_DIR = Paths.get("temp-audio");

    /**
     * Handles POST requests from the client's JavaScript to upload the recorded audio blob.
     * Saves the file locally and returns a RELATIVE path (not absolute file:/// URL).
     * This allows the backend to properly read and process the file later.
     * 
     * @param file The audio file (MultipartFile) sent from the browser.
     * @return A JSON object containing the relative fileUrl.
     */
    @PostMapping("/api/upload-audio")
    public ResponseEntity<Map<String, String>> uploadAudio(@RequestParam("audioFile") MultipartFile file) {

        // 1. Basic Validation
        if (file.isEmpty()) {
            System.err.println("Upload attempt received but file was empty.");
            return ResponseEntity.badRequest().body(Map.of("error", "The uploaded file is empty."));
        }

        try {
            // 2. Ensure the upload directory exists
            Files.createDirectories(UPLOAD_DIR);

            // 3. Create a unique filename (recommended for security)
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "recording";
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (!extension.equalsIgnoreCase(".webm")) {
                extension = ".webm"; // Force .webm extension if missing, based on browser output
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = UPLOAD_DIR.resolve(filename);

            // 4. Save the file to the local directory
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 5. ✅ Return RELATIVE path instead of absolute file:/// URL
            // This is much shorter and can be used by backend to locate the file
            String relativePath = "temp-audio/" + filename;

            System.out.println("File uploaded successfully to: " + filePath.toAbsolutePath());
            System.out.println("Returning relative path: " + relativePath);

            // Return both for flexibility (you can use either in your code)
            return ResponseEntity.ok(Map.of(
                "fileUrl", relativePath,           // ✅ Use this (short, portable)
                "fileName", filename                // Just the filename
            ));

        } catch (IOException e) {
            System.err.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Server failed to save file."));
        }
    }
}