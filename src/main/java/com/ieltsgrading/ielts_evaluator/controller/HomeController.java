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
     * @return index.html (1 trang chung cho cả guest và user)
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("pageTitle", "IELTS Test With AI");
        
        // Kiểm tra trạng thái đăng nhập từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated()
                && !auth.getName().equals("anonymousUser");
        
        if (isLoggedIn) {
            try {
                // Lấy user từ database dựa trên email
                String email = auth.getName();
                User user = userService.getUserByEmail(email);
                
                // Đồng bộ với session
                session.setAttribute("loggedInUser", user);
                
                // Thêm vào model để header hiển thị đúng
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("userName", user.getName());
                model.addAttribute("user", user);
                
                System.out.println("✅ User logged in: " + user.getName());
            } catch (Exception e) {
                // Nếu không tìm thấy user, clear session
                session.removeAttribute("loggedInUser");
                model.addAttribute("isLoggedIn", false);
                System.err.println("❌ Error loading user: " + e.getMessage());
            }
        } else {
            // Guest mode
            session.removeAttribute("loggedInUser");
            model.addAttribute("isLoggedIn", false);
            System.out.println("👤 Guest accessing homepage");
        }
        
        return "index"; // ✅ Trả về index.html cho cả guest và user
    }

    /**
     * Redirect /home về /
     * (Nếu có ai đó truy cập /home)
     */
    @GetMapping("/home")
    public String homeRedirect() {
        return "redirect:/";
    }

    /**
     * Redirect /index về /
     * (Nếu có ai đó truy cập /index)
     */
    @GetMapping("/index")
    public String indexRedirect() {
        return "redirect:/";
    }

    /**
     * Dashboard page - CHỈ cho user đã đăng nhập
     * Hiển thị thống kê cá nhân, kết quả test, ...
     * @return dashboard.html
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Kiểm tra Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            System.out.println("🚫 Unauthorized access to dashboard");
            return "redirect:/user/login?redirect=/dashboard";
        }
        
        try {
            // Lấy user từ database
            String email = auth.getName();
            User user = userService.getUserByEmail(email);
            
            // Đồng bộ session
            session.setAttribute("loggedInUser", user);
            
            model.addAttribute("pageTitle", "Dashboard - IELTS Test With AI");
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
            
            System.out.println("📊 Dashboard loaded for: " + user.getName());
            return "dashboard";
        } catch (Exception e) {
            // Nếu có lỗi, redirect về login
            session.removeAttribute("loggedInUser");
            System.err.println("❌ Dashboard error: " + e.getMessage());
            return "redirect:/user/login";
        }
    }

    /**
     * Require login page - Thông báo cần đăng nhập
     * @return require-login.html
     */
    @GetMapping("/require-login")
    public String requireLogin(Model model, HttpSession session) {
        // Nếu đã đăng nhập rồi, redirect về trang chủ
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            System.out.println("🔄 Already logged in, redirecting to homepage");
            return "redirect:/";
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
                // Ignore
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
                // Ignore
            }
        }
        
        return "contact";
    }

    /**
     * Helper method: Sync user to session from Spring Security
     * (Có thể dùng lại ở các controller khác)
     */
    private void syncUserToSession(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                User user = userService.getUserByEmail(auth.getName());
                session.setAttribute("loggedInUser", user);
                System.out.println("🔄 Session synced for: " + user.getName());
            } catch (Exception e) {
                session.removeAttribute("loggedInUser");
                System.err.println("❌ Sync failed: " + e.getMessage());
            }
        } else {
            session.removeAttribute("loggedInUser");
        }
    }
}