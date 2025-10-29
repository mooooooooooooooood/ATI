package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * Data access layer for User entity
 * 
 * @author ATI Team
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     * @param email User email
     * @return Optional User
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by email and password (for authentication)
     * @param email User email
     * @param password Encrypted password
     * @return Optional User
     */
    Optional<User> findByEmailAndPassword(String email, String password);

    /**
     * Find user by verification token
     * @param token Verification token
     * @return Optional User
     */
    Optional<User> findByVerificationToken(String token);

    /**
     * Find user by reset token
     * @param token Reset token
     * @return Optional User
     */
    Optional<User> findByResetToken(String token);

    /**
     * Check if email exists
     * @param email Email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users
     * @return List of active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find users by name containing (case insensitive)
     * @param name Name search term
     * @return List of users
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Find users created after a certain date
     * @param date Date threshold
     * @return List of users
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find users with email verified
     * @param isVerified Verification status
     * @return List of users
     */
    List<User> findByIsEmailVerified(Boolean isVerified);

    /**
     * Count total users
     * @return Total count
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    /**
     * Count active users
     * @return Active users count
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Get top users by test count
     * @param limit Number of users to return
     * @return List of top users
     */
    @Query("SELECT u FROM User u ORDER BY u.totalTestsTaken DESC")
    List<User> findTopUsersByTestCount();

    /**
     * Get users with average score above threshold
     * @param minScore Minimum average score
     * @return List of users
     */
    @Query("SELECT u FROM User u WHERE u.averageScore >= :minScore ORDER BY u.averageScore DESC")
    List<User> findUsersWithHighScores(@Param("minScore") Double minScore);

    /**
     * Search users by multiple criteria
     * @param searchTerm Search term for name or email
     * @return List of users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Delete inactive users older than specified days
     * @param date Date threshold
     */
    void deleteByIsActiveFalseAndCreatedAtBefore(LocalDateTime date);
}