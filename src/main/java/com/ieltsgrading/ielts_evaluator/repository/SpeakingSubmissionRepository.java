package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Speaking Submissions
 */
@Repository
public interface SpeakingSubmissionRepository extends JpaRepository<SpeakingSubmission, Long> {
    
    /**
     * Find submission by UUID
     */
    Optional<SpeakingSubmission> findBySubmissionUuid(String submissionUuid);
    
    /**
     * Find all submissions by user (ordered by submitted date desc)
     */
    List<SpeakingSubmission> findByUserOrderBySubmittedAtDesc(User user);
    
    /**
     * Find submissions by user and status
     */
    @Query("SELECT s FROM SpeakingSubmission s WHERE s.user.id = :userId AND s.status = :status ORDER BY s.submittedAt DESC")
    List<SpeakingSubmission> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * Count submissions by user
     */
    long countByUser(User user);
    
    /**
     * Count submissions by user and status
     */
    long countByUserAndStatus(User user, String status);
    
    /**
     * Get average score of completed submissions by user
     */
    @Query("SELECT AVG(s.overallScore) FROM SpeakingSubmission s WHERE s.user.id = :userId AND s.status = 'completed' AND s.overallScore IS NOT NULL")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
    
    /**
     * Find pending/processing submissions by user
     */
    @Query("SELECT s FROM SpeakingSubmission s WHERE s.user.id = :userId AND s.status IN ('pending', 'processing') ORDER BY s.submittedAt DESC")
    List<SpeakingSubmission> findPendingByUserId(@Param("userId") Long userId);
    
    /**
     * Find completed submissions by user
     */
    @Query("SELECT s FROM SpeakingSubmission s WHERE s.user.id = :userId AND s.status = 'completed' ORDER BY s.submittedAt DESC")
    List<SpeakingSubmission> findCompletedByUserId(@Param("userId") Long userId);
}