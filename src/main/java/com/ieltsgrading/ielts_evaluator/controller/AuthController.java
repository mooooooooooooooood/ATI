package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class AuthController {

    @Autowired
    private UserService userService;

    // ==================== Login ====================

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid email or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (expired != null) {
            model.addAttribute("error", "Your session has expired. Please login again.");
        }
        
        model.addAttribute("pageTitle", "Login - IELTS Test With AI");
        return "login";
    }

    // ==================== Signup ====================

    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        model.addAttribute("pageTitle", "Sign Up - IELTS Test With AI");
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate password confirmation
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
                return "redirect:/user/signup";
            }

            // Register user
            User user = userService.registerUser(name, email, password);
            
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Please check your email to verify your account.");
            return "redirect:/user/login";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/signup";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed. Please try again.");
            return "redirect:/user/signup";
        }
    }

    // ==================== Email Verification ====================

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam("token") String token,
            RedirectAttributes redirectAttributes) {
        
        try {
            userService.verifyEmail(token);
            redirectAttributes.addFlashAttribute("success", 
                "Email verified successfully! You can now log in.");
            return "redirect:/user/login";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired verification token.");
            return "redirect:/user/login";
        }
    }

    // ==================== Forgot Password ====================

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("pageTitle", "Forgot Password - IELTS Test With AI");
        return "forgot-password";
    }

    @PostMapping("user/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        
        try {
            String resetToken = userService.requestPasswordReset(email);
            
            // In production, send email with reset link
            // For now, just show success message
            redirectAttributes.addFlashAttribute("success", 
                "Password reset instructions have been sent to your email.");
            redirectAttributes.addFlashAttribute("resetToken", resetToken); // For testing only
            
            return "redirect:/user/forgot-password";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Email not found or error occurred.");
            return "redirect:/user/forgot-password";
        }
    }

    // ==================== Reset Password ====================

    @GetMapping("/reset-password")
    public String showResetPasswordPage(
            @RequestParam("token") String token,
            Model model) {
        
        model.addAttribute("token", token);
        model.addAttribute("pageTitle", "Reset Password - IELTS Test With AI");
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate password confirmation
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
                redirectAttributes.addAttribute("token", token);
                return "redirect:/user/reset-password";
            }

            // Reset password
            userService.resetPasswordWithToken(token, password);
            
            redirectAttributes.addFlashAttribute("success", 
                "Password reset successfully! You can now log in with your new password.");
            return "redirect:/user/login";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("token", token);
            return "redirect:/user/reset-password";
        }
    }

    // ==================== Logout ====================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login?logout=true";
    }

    // ==================== Dashboard (After Login) ====================

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        try {
            User user = userService.getUserByEmail(email);
            
            // Đồng bộ user vào session (để tương thích với code cũ)
            session.setAttribute("loggedInUser", user);
            
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Dashboard - IELTS Test With AI");
            model.addAttribute("isLoggedIn", true);
            
            return "dashboard";
        } catch (Exception e) {
            return "redirect:/user/login";
        }
    }

    // ==================== Require Login Page ====================

    @GetMapping("/require-login")
    public String showRequireLoginPage(
            @RequestParam(value = "redirect", required = false) String redirect,
            Model model) {
        
        model.addAttribute("redirectUrl", redirect != null ? redirect : "/dashboard");
        model.addAttribute("pageTitle", "Login Required - IELTS Test With AI");
        return "require-login";
    }
}