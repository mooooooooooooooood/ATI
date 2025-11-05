package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.ReadingTest;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.repository.ReadingTestRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reading/tests") //
public class ReadingTestController {

    @Autowired
    private ReadingTestRepository testRepository;

    @GetMapping
    public String getAllTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        String newRedirectPath = "/reading/tests"; // Store the new path

        if (user == null) {
            // CHANGE 2: Updated the redirect path to the new base path
            return "redirect:/require-login?redirect=" + newRedirectPath;
        }

        model.addAttribute("pageTitle", "Reading Tests");
        model.addAttribute("user", user);
        model.addAttribute("tests", testRepository.findAll());
        model.addAttribute("testCount", testRepository.count());

        return "reading-tests";
    }

    @GetMapping("/{id}") // This now maps to /reading/tests/{id}
    public String getTestDetail(@PathVariable("id") int id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            // CHANGE 3: Updated the redirect path for test details
            return "redirect:/require-login?redirect=/reading/tests/" + id;
        }

        ReadingTest test = testRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));

        model.addAttribute("test", test);

        return "test-details"; // Note: The view name is 'test-detail', not 'test-details'
    }
}