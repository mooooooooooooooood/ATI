package com.ieltsgrading.ielts_evaluator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ✅ Web Configuration for Static Resources
 * Allows serving uploaded audio files
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configure resource handlers to serve uploaded files
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded audio files from /uploads/audio/**
        registry.addResourceHandler("/uploads/audio/**")
                .addResourceLocations("file:uploads/audio/")
                .setCachePeriod(3600); // Cache for 1 hour
        
        System.out.println("✅ Static resource handler configured:");
        System.out.println("   URL pattern: /uploads/audio/**");
        System.out.println("   File location: uploads/audio/");
    }
}