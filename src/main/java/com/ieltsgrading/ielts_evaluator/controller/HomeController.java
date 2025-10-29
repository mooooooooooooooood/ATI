package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller
 * Handles homepage and dashboard routes
 */
@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    /**
     * Homepage - Hiển thị cho cả guest và user đã đăng nhập
     * @return index.html
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("pageTitle", "IELTS Test With AI");
        
        // Kiểm tra từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated() 
                && !auth.getName().equals("anonymousUser");
        
        if (isLoggedIn) {
            try {
                // Lấy user từ database dựa trên email từ Spring Security
                String email = auth.getName();
                User user = userService.getUserByEmail(email);
                
                // Đồng bộ với session (để tương thích với code cũ)
                session.setAttribute("loggedInUser", user);
                
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("userName", user.getName());
                model.addAttribute("user", user);
            } catch (Exception e) {
                // Nếu không tìm thấy user, clear session
                session.removeAttribute("loggedInUser");
                model.addAttribute("isLoggedIn", false);
            }
        } else {
            // Đảm bảo session cũng được clear
            session.removeAttribute("loggedInUser");
            model.addAttribute("isLoggedIn", false);
        }
        
        return "index";
    }

    /**
     * Dashboard page - Chỉ cho user đã đăng nhập
     * @return dashboard.html
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Kiểm tra Spring Security trước
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/user/login";
        }
        
        try {
            // Lấy user từ database
            String email = auth.getName();
            User user = userService.getUserByEmail(email);
            
            // Đồng bộ với session (để tương thích với code cũ)
            session.setAttribute("loggedInUser", user);
            
            model.addAttribute("pageTitle", "Dashboard - IELTS Test With AI");
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
            
            return "dashboard";
        } catch (Exception e) {
            // Nếu có lỗi, redirect về login
            session.removeAttribute("loggedInUser");
            return "redirect:/user/login";
        }
    }

    /**
     * Require login page - Thông báo cần đăng nhập
     * @return require-login.html
     */
    @GetMapping("/require-login")
    public String requireLogin(Model model, HttpSession session) {
        // Kiểm tra xem user đã đăng nhập chưa
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Nếu đã đăng nhập, redirect về dashboard
            return "redirect:/dashboard";
        }
        
        model.addAttribute("pageTitle", "Login Required - IELTS Test With AI");
        return "require-login";
    }

    /**
     * About page
     * @return about.html
     */
    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        // Kiểm tra trạng thái đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated() 
                && !auth.getName().equals("anonymousUser");
        
        model.addAttribute("pageTitle", "About Us - IELTS Test With AI");
        model.addAttribute("isLoggedIn", isLoggedIn);
        
        if (isLoggedIn) {
            try {
                User user = userService.getUserByEmail(auth.getName());
                session.setAttribute("loggedInUser", user);
                model.addAttribute("userName", user.getName());
            } catch (Exception e) {
                // Ignore if user not found
            }
        }
        
        return "about";
    }

    /**
     * Contact page
     * @return contact.html
     */
    @GetMapping("/contact")
    public String contact(Model model, HttpSession session) {
        // Kiểm tra trạng thái đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated() 
                && !auth.getName().equals("anonymousUser");
        
        model.addAttribute("pageTitle", "Contact Us - IELTS Test With AI");
        model.addAttribute("isLoggedIn", isLoggedIn);
        
        if (isLoggedIn) {
            try {
                User user = userService.getUserByEmail(auth.getName());
                session.setAttribute("loggedInUser", user);
                model.addAttribute("userName", user.getName());
            } catch (Exception e) {
                // Ignore if user not found
            }
        }
        
        return "contact";
    }

    /**
     * Helper method: Sync user to session from Spring Security
     * Có thể gọi từ các controller khác nếu cần
     */
    private void syncUserToSession(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                User user = userService.getUserByEmail(auth.getName());
                session.setAttribute("loggedInUser", user);
            } catch (Exception e) {
                session.removeAttribute("loggedInUser");
            }
        } else {
            session.removeAttribute("loggedInUser");
        }
    }
}