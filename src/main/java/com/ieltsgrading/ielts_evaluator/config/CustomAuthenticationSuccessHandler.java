package com.ieltsgrading.ielts_evaluator.config;

import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Authentication Success Handler
 * Đồng bộ User object vào session sau khi đăng nhập thành công
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        // Lấy email từ authentication
        String email = authentication.getName();
        
        try {
            // Lấy user từ database
            User user = userService.getUserByEmail(email);
            
            // Đồng bộ user vào session (để tương thích với code cũ)
            HttpSession session = request.getSession();
            session.setAttribute("loggedInUser", user);
            
            // Update last login
            user.setLastLogin(java.time.LocalDateTime.now());
            user.incrementLoginCount();
            
            System.out.println("User logged in successfully: " + email);
            
        } catch (Exception e) {
            System.err.println("Error syncing user to session: " + e.getMessage());
        }
        
        // Redirect to dashboard
        response.sendRedirect("/dashboard");
    }
}