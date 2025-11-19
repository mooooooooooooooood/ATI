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
     * UPDATED: Now redirects to the dedicated ListeningTestController
     */
    @GetMapping("/listening")
    public String listeningTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        // Point to the NEW controller we created
        String redirectTarget = "/listening/tests";

        if (user == null) {
            return "redirect:/require-login?redirect=" + redirectTarget;
        }

        // CRITICAL FIX: Redirect to the controller that actually loads the database data
        return "redirect:" + redirectTarget;
    }

    /**
     * Speaking tests page
     * @return speaking-tests.html
     */
    @GetMapping("/speaking") // Path: /test/speaking
    public String speakingTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        String redirectTarget = "/speaking/tests";

        if (user == null) {
            return "redirect:/require-login?redirect=" + redirectTarget;
        }

        return "redirect:" + redirectTarget;
    }

    /**
     * Reading tests page
     * @return reading-tests.html
     */
    @GetMapping("/reading") // Path: /test/reading
    public String readingTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        String redirectTarget = "/reading/tests";

        if (user == null) {
            return "redirect:/require-login?redirect=" + redirectTarget;
        }

        return "redirect:" + redirectTarget;
    }
}