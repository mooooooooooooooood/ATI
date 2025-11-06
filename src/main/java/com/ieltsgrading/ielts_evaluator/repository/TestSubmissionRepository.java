package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.TestSubmission;
import com.ieltsgrading.ielts_evaluator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestSubmissionRepository extends JpaRepository<TestSubmission, Long> {
    
    /**
     * Find submission by UUID
     */
    Optional<TestSubmission> findBySubmissionUuid(String submissionUuid);
    
    /**
     * Find all submissions by user (ordered by submitted date desc)
     */
    List<TestSubmission> findByUserOrderBySubmittedAtDesc(User user);
    
    /**
     * Find submissions by user and status
     */
    List<TestSubmission> findByUserAndStatusOrderBySubmittedAtDesc(User user, String status);
    
    /**
     * Find all submissions by user ID
     */
    @Query("SELECT s FROM TestSubmission s WHERE s.user.id = :userId ORDER BY s.submittedAt DESC")
    List<TestSubmission> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find pending/processing submissions by user
     */
    @Query("SELECT s FROM TestSubmission s WHERE s.user.id = :userId AND s.status IN ('pending', 'processing') ORDER BY s.submittedAt DESC")
    List<TestSubmission> findPendingByUserId(@Param("userId") Long userId);
    
    /**
     * Find completed submissions by user
     */
    @Query("SELECT s FROM TestSubmission s WHERE s.user.id = :userId AND s.status = 'completed' ORDER BY s.submittedAt DESC")
    List<TestSubmission> findCompletedByUserId(@Param("userId") Long userId);
    
    /**
     * Count submissions by user
     */
    long countByUser(User user);
    
    /**
     * Count completed submissions by user
     */
    long countByUserAndStatus(User user, String status);
    
    /**
     * Get average score of completed submissions by user
     */
    @Query("SELECT AVG(s.overallScore) FROM TestSubmission s WHERE s.user.id = :userId AND s.status = 'completed' AND s.overallScore IS NOT NULL")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
    
    /**
     * Get latest submission by user
     */
    @Query("SELECT s FROM TestSubmission s WHERE s.user.id = :userId ORDER BY s.submittedAt DESC LIMIT 1")
    Optional<TestSubmission> findLatestByUserId(@Param("userId") Long userId);
}