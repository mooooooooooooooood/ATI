package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

/**
 * Test Controller
 * Handles all test routes (Writing, Listening, Speaking, Reading)
 * Requires authentication
 */
@Controller
@RequestMapping("/test")
public class TestController {

    /**
     * Test list page - Hiển thị danh sách các loại test
     * @return test-list.html hoặc require-login.html
     */
    @GetMapping("/list")
    public String testList(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        // Check if user is logged in
        if (user == null) {
            return "redirect:/require-login?redirect=/test/list";
        }
        
        model.addAttribute("pageTitle", "Available Tests");
        model.addAttribute("user", user);
        
        return "test-list";
    }

    /**
     * Writing tests page
     * @return writing-tests.html
     */
    @GetMapping("/writing")
    public String writingTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/require-login?redirect=/test/writing";
        }
        
        model.addAttribute("pageTitle", "Writing Tests");
        model.addAttribute("user", user);
        model.addAttribute("testCount", 30);
        
        return "writing-tests";
    }

    /**
     * Listening tests page
     * @return listening-tests.html
     */
    @GetMapping("/listening")
    public String listeningTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/require-login?redirect=/test/listening";
        }
        
        model.addAttribute("pageTitle", "Listening Tests");
        model.addAttribute("user", user);
        model.addAttribute("testCount", 40);
        
        return "listening-tests";
    }

    /**
     * Speaking tests page
     * @return speaking-tests.html
     */
    @GetMapping("/speaking")
    public String speakingTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/require-login?redirect=/test/speaking";
        }
        
        model.addAttribute("pageTitle", "Speaking Tests");
        model.addAttribute("user", user);
        model.addAttribute("testCount", 50);
        
        return "speaking-tests";
    }

    /**
     * Reading tests page
     * @return reading-tests.html
     */
    @GetMapping("/reading") // Path: /test/reading
    public String readingTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        // This is the new, correct path handled by the dedicated controller
        String redirectTarget = "/test/reading";

        if (user == null) {
            // Redirect unauthenticated user to log in, preserving the target path
            return "redirect:/require-login?redirect=" + redirectTarget;
        }

        // FIX: Redirects the browser to the dedicated controller path /reading/tests
        return "redirect:" + redirectTarget;
    }
}