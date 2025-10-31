package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.util.*;

/**
 * User Controller - Profile Management Only
 * Handles user profile management, avatar upload, and account settings
 *
 * @author ATI Team
 * @version 1.0
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // Session attribute keys
    private static final String SESSION_USER = "loggedInUser";
    private static final String SESSION_USER_ID = "userId";

    /**
     * Display user profile page
     *
     * @param model Spring Model
     * @param session HTTP Session
     * @return profile.html
     */
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        
        if (user == null) {
            return "redirect:/user/login";
        }
        
        // Refresh user data from database
        try {
            user = userService.getUserById(user.getId());
            session.setAttribute(SESSION_USER, user);
            
            // Get user statistics
            Map<String, Object> stats = userService.getUserStatistics(user.getId());
            model.addAttribute("statistics", stats);
            
        } catch (Exception e) {
            System.err.println("Error loading user profile: " + e.getMessage());
        }
        
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Update user profile
     *
     * @param name User name
     * @param phone User phone
     * @param country User country
     * @param targetScore User target IELTS score
     * @param session HTTP Session
     * @param model Spring Model
     * @return Redirect to profile with message
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Double targetScore,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute(SESSION_USER);
        
        if (user == null) {
            return "redirect:/user/login";
        }
        
        try {
            // Update user profile using UserService method
            User updatedUser = userService.updateProfile(
                user.getId(),
                name,
                phone,
                country,
                targetScore
            );
            
            // Update session
            session.setAttribute(SESSION_USER, updatedUser);
            session.setAttribute("userName", updatedUser.getName());
            
            model.addAttribute("success", "Profile updated successfully");
            model.addAttribute("user", updatedUser);
            
            // Get updated statistics
            Map<String, Object> stats = userService.getUserStatistics(updatedUser.getId());
            model.addAttribute("statistics", stats);
            
            System.out.println("Profile updated for user: " + updatedUser.getEmail());
            
            return "profile";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
            model.addAttribute("user", user);
            System.err.println("Profile update error: " + e.getMessage());
            return "profile";
        }
    }

    /**
     * Change password
     *
     * @param currentPassword Current password
     * @param newPassword New password
     * @param confirmPassword Confirm new password
     * @param session HTTP Session
     * @param model Spring Model
     * @return Redirect to profile with message
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute(SESSION_USER);
        
        if (user == null) {
            return "redirect:/user/login";
        }
        
        try {
            // Validate password confirmation
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("New passwords do not match");
            }
            
            // Change password using UserService method
            userService.changePassword(user.getId(), currentPassword, newPassword);
            
            model.addAttribute("success", "Password changed successfully");
            model.addAttribute("user", user);
            
            // Get statistics
            Map<String, Object> stats = userService.getUserStatistics(user.getId());
            model.addAttribute("statistics", stats);
            
            System.out.println("Password changed for user: " + user.getEmail());
            
            return "profile";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to change password. Please try again.");
            model.addAttribute("user", user);
            System.err.println("Password change error: " + e.getMessage());
            return "profile";
        }
    }

    /**
     * Upload avatar/profile picture
     *
     * @param file Avatar file
     * @param session HTTP Session
     * @return ResponseEntity with result
     */
    @PostMapping("/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        
        User user = (User) session.getAttribute(SESSION_USER);
        Map<String, String> response = new HashMap<>();
        
        if (user == null) {
            response.put("error", "User not logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Please select a file to upload");
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }
            
            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must not exceed 5MB");
            }
            
            // TODO: Implement file upload service to save file
            // For now, generate a mock URL
            String avatarUrl = "/uploads/avatars/" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
            
            // Update avatar using UserService method
            User updatedUser = userService.updateAvatar(user.getId(), avatarUrl);
            
            // Update user in session
            session.setAttribute(SESSION_USER, updatedUser);
            
            response.put("success", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);
            
            System.out.println("Avatar uploaded for user: " + user.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Failed to upload avatar");
            System.err.println("Avatar upload error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deactivate user account
     *
     * @param password User password for confirmation
     * @param session HTTP Session
     * @param model Spring Model
     * @return Redirect to login or profile with error
     */
    @PostMapping("/profile/deactivate")
    public String deactivateAccount(
            @RequestParam String password,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute(SESSION_USER);
        
        if (user == null) {
            return "redirect:/user/login";
        }
        
        try {
            // Verify password before deactivation
            userService.authenticateUser(user.getEmail(), password);
            
            // Deactivate account using UserService method
            userService.deactivateAccount(user.getId());
            
            // Logout
            session.invalidate();
            
            System.out.println("Account deactivated for user: " + user.getEmail());
            
            return "redirect:/user/login?deactivated=success";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Incorrect password");
            model.addAttribute("user", user);
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to deactivate account. Please try again.");
            model.addAttribute("user", user);
            System.err.println("Account deactivation error: " + e.getMessage());
            return "profile";
        }
    }

    /**
     * Delete user account permanently
     *
     * @param password User password for confirmation
     * @param session HTTP Session
     * @param model Spring Model
     * @return Redirect to login or profile with error
     */
    @PostMapping("/profile/delete")
    public String deleteAccount(
            @RequestParam String password,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute(SESSION_USER);
        
        if (user == null) {
            return "redirect:/user/login";
        }
        
        try {
            // Verify password before deletion
            userService.authenticateUser(user.getEmail(), password);
            
            // Delete account using UserService method
            userService.deleteAccount(user.getId());
            
            // Logout
            session.invalidate();
            
            System.out.println("Account deleted for user: " + user.getEmail());
            
            return "redirect:/user/login?deleted=success";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Incorrect password");
            model.addAttribute("user", user);
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete account. Please try again.");
            model.addAttribute("user", user);
            System.err.println("Account deletion error: " + e.getMessage());
            return "profile";
        }
    }

    /**
     * Get current user info (AJAX)
     *
     * @param session HTTP Session
     * @return ResponseEntity with user info
     */
    @GetMapping("/current")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("country", user.getCountry());
        response.put("avatar", user.getAvatar());
        response.put("targetScore", user.getTargetScore());
        response.put("isActive", user.getIsActive());
        response.put("isEmailVerified", user.getIsEmailVerified());
        response.put("createdAt", user.getCreatedAt());
        response.put("lastLogin", user.getLastLogin());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics (AJAX)
     *
     * @param session HTTP Session
     * @return ResponseEntity with statistics
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStatistics(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>());
        }
        
        try {
            Map<String, Object> stats = userService.getUserStatistics(user.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error getting user statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }
}