package com.ieltsgrading.ielts_evaluator.service;

import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * User Service - Complete Implementation
 * Business logic for user management
 * 
 * @author ATI Team
 * @version 1.0
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Constants
    private static final int RESET_TOKEN_EXPIRY_HOURS = 24;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Register new user
     * 
     * @param name User's full name
     * @param email User's email
     * @param password User's password
     * @return Created user
     * @throws IllegalArgumentException if validation fails
     */
    public User registerUser(String name, String email, String password) {
        // Validate input
        validateUserInput(name, email, password);

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        // Create new user
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        
        // Generate verification token
        user.setVerificationToken(generateToken());

        // Set default role
        user.addRole("ROLE_USER");

        // Save user
        User savedUser = userRepository.save(user);
        
        // TODO: Send verification email
        // emailService.sendVerificationEmail(savedUser);

        System.out.println("User registered successfully: " + email);
        return savedUser;
    }

    /**
     * Authenticate user
     * 
     * @param email User's email
     * @param password User's password
     * @return Authenticated user
     * @throws IllegalArgumentException if authentication fails
     */
    public User authenticateUser(String email, String password) {
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOptional.get();

        // Check if account is active
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Update login information
        user.setLastLogin(LocalDateTime.now());
        user.incrementLoginCount();
        userRepository.save(user);

        System.out.println("User authenticated: " + email);
        return user;
    }

    /**
     * Get user by ID
     * 
     * @param userId User ID
     * @return User
     * @throws NoSuchElementException if user not found
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
    }

    /**
     * Get user by email
     * 
     * @param email User's email
     * @return User
     * @throws NoSuchElementException if user not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));
    }

    /**
     * Update user profile
     * 
     * @param userId User ID
     * @param name New name
     * @param phone New phone
     * @param country New country
     * @param targetScore New target score
     * @return Updated user
     */
    public User updateProfile(Long userId, String name, String phone, String country, Double targetScore) {
        User user = getUserById(userId);

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }

        if (phone != null) {
            user.setPhone(phone.trim());
        }

        if (country != null) {
            user.setCountry(country.trim());
        }

        if (targetScore != null && targetScore >= 0 && targetScore <= 9) {
            user.setTargetScore(targetScore);
        }

        User updatedUser = userRepository.save(user);
        System.out.println("Profile updated for user: " + userId);
        return updatedUser;
    }

    /**
     * Change user password
     * 
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @throws IllegalArgumentException if validation fails
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        // Check if new password is same as old
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        System.out.println("Password changed for user: " + userId);
    }

    /**
     * Request password reset
     * 
     * @param email User's email
     * @return Reset token
     */
    public String requestPasswordReset(String email) {
        User user = getUserByEmail(email);

        // Generate reset token
        String resetToken = generateToken();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS));

        userRepository.save(user);

        // TODO: Send reset email
        // emailService.sendPasswordResetEmail(user, resetToken);

        System.out.println("Password reset requested for: " + email);
        return resetToken;
    }

    /**
     * Reset password with token
     * 
     * @param resetToken Reset token
     * @param newPassword New password
     * @throws IllegalArgumentException if token is invalid or expired
     */
    public void resetPasswordWithToken(String resetToken, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetToken(resetToken);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid reset token");
        }

        User user = userOptional.get();

        // Check if token is expired
        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        System.out.println("Password reset successfully for user: " + user.getId());
    }

    /**
     * Verify email with token
     * 
     * @param verificationToken Verification token
     * @throws IllegalArgumentException if token is invalid
     */
    public void verifyEmail(String verificationToken) {
        Optional<User> userOptional = userRepository.findByVerificationToken(verificationToken);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid verification token");
        }

        User user = userOptional.get();
        user.setIsEmailVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        System.out.println("Email verified for user: " + user.getId());
    }

    /**
     * Update user avatar
     * 
     * @param userId User ID
     * @param avatarUrl Avatar URL
     * @return Updated user
     */
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = getUserById(userId);
        user.setAvatar(avatarUrl);
        
        User updatedUser = userRepository.save(user);
        System.out.println("Avatar updated for user: " + userId);
        return updatedUser;
    }

    /**
     * Deactivate user account
     * 
     * @param userId User ID
     */
    public void deactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);

        System.out.println("Account deactivated for user: " + userId);
    }

    /**
     * Activate user account
     * 
     * @param userId User ID
     */
    public void activateAccount(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);

        System.out.println("Account activated for user: " + userId);
    }

    /**
     * Delete user account permanently
     * 
     * @param userId User ID
     */
    public void deleteAccount(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);

        System.out.println("Account deleted for user: " + userId);
    }

    /**
     * Get user statistics
     * 
     * @param userId User ID
     * @return Map with statistics
     */
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = getUserById(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", user.getId());
        stats.put("name", user.getName());
        stats.put("email", user.getEmail());
        stats.put("totalTestsTaken", user.getTotalTestsTaken());
        stats.put("averageScore", user.getAverageScore());
        stats.put("targetScore", user.getTargetScore());
        stats.put("memberSince", user.getCreatedAt());
        stats.put("lastLogin", user.getLastLogin());
        stats.put("loginCount", user.getLoginCount());
        stats.put("isEmailVerified", user.getIsEmailVerified());

        // TODO: Add more detailed statistics from test results
        // stats.put("writingAverage", testResultService.getAverageScore(userId, "writing"));
        // stats.put("speakingAverage", testResultService.getAverageScore(userId, "speaking"));
        // stats.put("listeningAverage", testResultService.getAverageScore(userId, "listening"));
        // stats.put("readingAverage", testResultService.getAverageScore(userId, "reading"));

        return stats;
    }

    /**
     * Update user test statistics
     * 
     * @param userId User ID
     * @param newScore New test score
     */
    public void updateTestStatistics(Long userId, Double newScore) {
        User user = getUserById(userId);

        int totalTests = user.getTotalTestsTaken();
        double currentAvg = user.getAverageScore();

        // Calculate new average
        double newAverage = ((currentAvg * totalTests) + newScore) / (totalTests + 1);

        user.incrementTestsTaken();
        user.setAverageScore(Math.round(newAverage * 10.0) / 10.0);

        userRepository.save(user);

        System.out.println("Test statistics updated for user: " + userId);
    }

    /**
     * Search users
     * 
     * @param searchTerm Search term
     * @return List of users
     */
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.searchUsers(searchTerm.trim());
    }

    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get active users
     * 
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    /**
     * Get top users by test count
     * 
     * @param limit Number of users to return
     * @return List of top users
     */
    public List<User> getTopUsers(int limit) {
        List<User> allUsers = userRepository.findTopUsersByTestCount();
        return allUsers.size() > limit ? allUsers.subList(0, limit) : allUsers;
    }

    /**
     * Count total users
     * 
     * @return Total user count
     */
    public long getTotalUserCount() {
        return userRepository.countTotalUsers();
    }

    /**
     * Count active users
     * 
     * @return Active user count
     */
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    // ==================== Helper Methods ====================

    /**
     * Validate user input
     */
    private void validateUserInput(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Generate random token
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}