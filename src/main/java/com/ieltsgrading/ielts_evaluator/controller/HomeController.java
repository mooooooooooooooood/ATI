package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.*;
import com.ieltsgrading.ielts_evaluator.repository.*;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private WritingSubmissionRepository writingSubmissionRepository;

    @Autowired(required = false)
    private SpeakingSubmissionRepository speakingSubmissionRepository;

    @Autowired(required = false)
    private ListeningSubmissionRepository listeningSubmissionRepository;

    @Autowired(required = false)
    private ReadingSubmissionRepository readingSubmissionRepository;

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
            } catch (Exception e) {
                session.removeAttribute("loggedInUser");
                model.addAttribute("isLoggedIn", false);
            }
        } else {
            session.removeAttribute("loggedInUser");
            model.addAttribute("isLoggedIn", false);
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
     * Dashboard - Lấy submissions từ 4 tables khác nhau
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/user/login?redirect=/dashboard";
        }

        try {
            String email = auth.getName();
            User user = userService.getUserByEmail(email);
            session.setAttribute("loggedInUser", user);

            // 🔥 GET SUBMISSIONS FROM EACH TABLE
            List<ITestSubmission> writingSubmissions = new ArrayList<>();
            List<ITestSubmission> speakingSubmissions = new ArrayList<>();
            List<ITestSubmission> listeningSubmissions = new ArrayList<>();
            List<ITestSubmission> readingSubmissions = new ArrayList<>();

            // Writing submissions
            List<WritingSubmission> writingList = writingSubmissionRepository
                    .findByUserOrderBySubmittedAtDesc(user);
            writingSubmissions.addAll(writingList);

            // Speaking submissions (nếu có repository)
            if (speakingSubmissionRepository != null) {
                List<SpeakingSubmission> speakingList = speakingSubmissionRepository
                        .findByUserOrderBySubmittedAtDesc(user);
                speakingSubmissions.addAll(speakingList);
            }

            // Listening submissions (nếu có repository)
            if (listeningSubmissionRepository != null) {
                List<ListeningSubmission> listeningList = listeningSubmissionRepository
                        .findByUserOrderBySubmittedAtDesc(user);
                listeningSubmissions.addAll(listeningList);
            }

            // Reading submissions (nếu có repository)
            if (readingSubmissionRepository != null) {
                List<ReadingSubmission> readingList = readingSubmissionRepository
                        .findByUserOrderBySubmittedAtDesc(user);
                readingSubmissions.addAll(readingList);
            }

            // Calculate statistics
            long totalTests = writingSubmissions.size() + speakingSubmissions.size() +
                    listeningSubmissions.size() + readingSubmissions.size();

            long completedTests = countCompleted(writingSubmissions) +
                    countCompleted(speakingSubmissions) +
                    countCompleted(listeningSubmissions) +
                    countCompleted(readingSubmissions);

            long processingTests = countProcessing(writingSubmissions) +
                    countProcessing(speakingSubmissions) +
                    countProcessing(listeningSubmissions) +
                    countProcessing(readingSubmissions);

            double averageScore = calculateAverageScore(
                    writingSubmissions, speakingSubmissions,
                    listeningSubmissions, readingSubmissions);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTests", totalTests);
            stats.put("completedTests", completedTests);
            stats.put("processingTests", processingTests);
            stats.put("averageScore", averageScore);

            // Add to model
            model.addAttribute("pageTitle", "Dashboard - IELTS Test With AI");
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("writingSubmissions", writingSubmissions);
            model.addAttribute("speakingSubmissions", speakingSubmissions);
            model.addAttribute("listeningSubmissions", listeningSubmissions);
            model.addAttribute("readingSubmissions", readingSubmissions);
            model.addAttribute("stats", stats);

            System.out.println("📊 Dashboard loaded:");
            System.out.println("   Writing: " + writingSubmissions.size());
            System.out.println("   Speaking: " + speakingSubmissions.size());
            System.out.println("   Listening: " + listeningSubmissions.size());
            System.out.println("   Reading: " + readingSubmissions.size());

            return "dashboard";
        } catch (Exception e) {
            System.err.println("❌ Dashboard error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/user/login";
        }
    }

    /**
     * View result - Route dựa trên submission UUID prefix hoặc query parameter
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

            // 🔥 TÌM SUBMISSION TRONG TẤT CẢ TABLES
            ITestSubmission submission = findSubmissionByUuid(submissionUuid, user);

            if (submission == null) {
                model.addAttribute("error", "Submission not found");
                return "error-404";
            }

            // Check ownership
            if (!submission.getUserId().equals(user.getId())) {
                model.addAttribute("error", "Access denied");
                return "error-403";
            }

            // Common model attributes
            model.addAttribute("pageTitle", "Test Result - " + submission.getTestDisplayName());
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("submission", submission);

            // Route to appropriate template
            String testType = submission.getTestType().toLowerCase();
            System.out.println("📄 Loading result for: " + testType);

            switch (testType) {
                case "writing":
                    return "writing-result";
                case "speaking":
                    return "speaking-result";
                case "listening":
                    return "listening-result";
                case "reading":
                    return "reading-result";
                default:
                    return "test-result";
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading result: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading result");
            return "error";
        }
    }

    @GetMapping("/require-login")
    public String requireLogin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        model.addAttribute("pageTitle", "Login Required");
        return "require-login";
    }

    /* ==================== HELPER METHODS ==================== */

    private ITestSubmission findSubmissionByUuid(String uuid, User user) {
        // Try writing
        Optional<WritingSubmission> writing = writingSubmissionRepository
                .findBySubmissionUuid(uuid);
        if (writing.isPresent() && writing.get().getUser().getId().equals(user.getId())) {
            return writing.get();
        }

        // Try speaking
        if (speakingSubmissionRepository != null) {
            Optional<SpeakingSubmission> speaking = speakingSubmissionRepository
                    .findBySubmissionUuid(uuid);
            if (speaking.isPresent() && speaking.get().getUser().getId().equals(user.getId())) {
                return speaking.get();
            }
        }

        // Try listening
        if (listeningSubmissionRepository != null) {
            Optional<ListeningSubmission> listening = listeningSubmissionRepository
                    .findBySubmissionUuid(uuid);
            if (listening.isPresent() && listening.get().getUser().getId().equals(user.getId())) {
                return listening.get();
            }
        }

        // Try reading
        if (readingSubmissionRepository != null) {
            Optional<ReadingSubmission> reading = readingSubmissionRepository
                    .findBySubmissionUuid(uuid);
            if (reading.isPresent() && reading.get().getUser().getId().equals(user.getId())) {
                return reading.get();
            }
        }

        return null;
    }

    private long countCompleted(List<ITestSubmission> submissions) {
        return submissions.stream().filter(ITestSubmission::isCompleted).count();
    }

    private long countProcessing(List<ITestSubmission> submissions) {
        return submissions.stream()
                .filter(s -> s.isProcessing() || s.isPending())
                .count();
    }

    private double calculateAverageScore(List<ITestSubmission>... submissionLists) {
        double totalScore = 0;
        int count = 0;

        for (List<ITestSubmission> list : submissionLists) {
            for (ITestSubmission sub : list) {
                if (sub.isCompleted() && sub.getOverallScore() != null) {
                    totalScore += sub.getOverallScore();
                    count++;
                }
            }
        }

        return count > 0 ? Math.round(totalScore / count * 10.0) / 10.0 : 0.0;
    }
}