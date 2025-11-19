package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.*;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingSubmissionDetail;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingSubmissionDetailRepository;
import com.ieltsgrading.ielts_evaluator.service.TestSubmissionService;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ✅ UPDATED: Home Controller with Speaking Submission Detail Support
 */
@Controller
public class HomeController {
    
    @Autowired
    private TestSubmissionService submissionService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private SpeakingSubmissionDetailRepository speakingDetailRepo;

    /**
     * Homepage
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("pageTitle", "IELTS Test With AI");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated()
                && !auth.getName().equals("anonymousUser");

        if (isLoggedIn) {
            try {
                String email = auth.getName();
                User user = userService.getUserByEmail(email);
                session.setAttribute("loggedInUser", user);
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("userName", user.getName());
                model.addAttribute("user", user);
                System.out.println("✅ User logged in: " + user.getName());
            } catch (Exception e) {
                session.removeAttribute("loggedInUser");
                model.addAttribute("isLoggedIn", false);
                System.err.println("❌ Error loading user: " + e.getMessage());
            }
        } else {
            session.removeAttribute("loggedInUser");
            model.addAttribute("isLoggedIn", false);
            System.out.println("👤 Guest accessing homepage");
        }

        return "index";
    }

    @GetMapping("/home")
    public String homeRedirect() {
        return "redirect:/";
    }

    @GetMapping("/index")
    public String indexRedirect() {
        return "redirect:/";
    }

    /**
     * Dashboard page - Display user's test submissions
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            System.out.println("🚫 Unauthorized access to dashboard");
            return "redirect:/user/login?redirect=/dashboard";
        }

        try {
            String email = auth.getName();
            User user = userService.getUserByEmail(email);
            session.setAttribute("loggedInUser", user);

            List<ITestSubmission> allSubmissions = submissionService.getUserSubmissions(user);
            
            List<ITestSubmission> processingSubmissions = new ArrayList<>();
            List<ITestSubmission> completedSubmissions = new ArrayList<>();

            for (ITestSubmission submission : allSubmissions) {
                if (submission.isCompleted()) {
                    completedSubmissions.add(submission);
                } else if (submission.isProcessing() || submission.isPending()) {
                    processingSubmissions.add(submission);
                }
            }

            Map<String, Object> stats = submissionService.getUserStats(user);

            model.addAttribute("pageTitle", "Dashboard - IELTS Test With AI");
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("processingSubmissions", processingSubmissions);
            model.addAttribute("completedSubmissions", completedSubmissions);
            model.addAttribute("stats", stats);

            System.out.println("📊 Dashboard loaded for: " + user.getName());
            System.out.println("   Total submissions: " + allSubmissions.size());
            System.out.println("   Processing: " + processingSubmissions.size());
            System.out.println("   Completed: " + completedSubmissions.size());

            return "dashboard";
        } catch (Exception e) {
            session.removeAttribute("loggedInUser");
            System.err.println("❌ Dashboard error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/user/login";
        }
    }

    /**
     * ✅ UPDATED: View test result detail - Universal route for all test types
     */
    @GetMapping("/result/{submissionUuid}")
    public String viewResult(
            @PathVariable String submissionUuid,
            Model model,
            HttpSession session) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/user/login";
        }

        try {
            User user = userService.getUserByEmail(auth.getName());
            
            Optional<ITestSubmission> optSubmission = submissionService.getSubmission(submissionUuid);

            if (optSubmission.isEmpty()) {
                model.addAttribute("error", "Submission not found");
                return "error-404";
            }

            ITestSubmission submission = optSubmission.get();

            // Check ownership
            if (!submission.getUserId().equals(user.getId())) {
                model.addAttribute("error", "Access denied");
                return "error-403";
            }

            model.addAttribute("pageTitle", "Test Result - " + submission.getTestDisplayName());
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("submission", submission);

            // Route to appropriate result page based on test type
            if ("writing".equals(submission.getTestType())) {
                if (submission.isCompleted()) {
                    WritingSubmission ws = (WritingSubmission) submission;
                    if (ws.getTask1Result() != null) {
                        Map<String, Object> task1Detail = submissionService
                                .parseDetailedResult(ws.getTask1Result());
                        model.addAttribute("task1Detail", task1Detail);
                    }
                    if (ws.getTask2Result() != null) {
                        Map<String, Object> task2Detail = submissionService
                                .parseDetailedResult(ws.getTask2Result());
                        model.addAttribute("task2Detail", task2Detail);
                    }
                }
                return "writing-result";
                
            } else if ("speaking".equals(submission.getTestType())) {
                // ✅ Load speaking submission detail from database
                if (submission.isCompleted()) {
                    SpeakingSubmission ss = (SpeakingSubmission) submission;
                    
                    // Try to get from SpeakingSubmissionDetail table first
                    Optional<SpeakingSubmissionDetail> detailOpt = speakingDetailRepo
                            .findBySubmission_SubmissionId(ss.getSubmissionId());
                    
                    if (detailOpt.isPresent()) {
                        // ✅ Use structured data from detail table
                        SpeakingSubmissionDetail detail = detailOpt.get();
                        model.addAttribute("speakingDetail", detail);
                        
                        System.out.println("✅ Loaded speaking detail from database");
                        System.out.println("   Fluency: " + detail.getFluency());
                        System.out.println("   Lexical: " + detail.getLexicalResource());
                        
                    } else if (ss.getSpeakingResult() != null) {
                        // ✅ Fallback: Parse from JSON (for old submissions)
                        Map<String, Object> speakingDetail = submissionService
                                .parseDetailedResult(ss.getSpeakingResult());
                        model.addAttribute("speakingDetail", speakingDetail);
                        
                        System.out.println("⚠️ Using JSON fallback for speaking detail");
                    }
                }
                return "speaking/speaking-result";
                
            } else {
                model.addAttribute("error", "Unknown test type: " + submission.getTestType());
                return "error";
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading result: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading result");
            return "error";
        }
    }

    /**
     * Require login page
     */
    @GetMapping("/require-login")
    public String requireLogin(Model model, HttpSession session) {
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
}