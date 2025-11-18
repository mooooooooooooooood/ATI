package com.ieltsgrading.ielts_evaluator.repository.speaking;

import com.ieltsgrading.ielts_evaluator.model.SpeakingSubmission;
import com.ieltsgrading.ielts_evaluator.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeakingSubmissionRepository extends JpaRepository<SpeakingSubmission, Long> {
    
    Optional<SpeakingSubmission> findBySubmissionUuid(String submissionUuid);
    
    List<SpeakingSubmission> findByUser_IdOrderBySubmittedAtDesc(Long userId);
    
    List<SpeakingSubmission> findByUser_IdAndStatus(Long userId, String status);
    
    long countByUser_IdAndStatus(Long userId, String status);

    long countByUser(User user);

    long countByUserAndStatus(User user, String status);

    List<SpeakingSubmission> findByUserOrderBySubmittedAtDesc(User user);

    long countByUser_Id(Long id);
    
    // ✅ NEW: Added missing methods
    
    /**
     * Find submissions by User ID and Status
     * Used in getPendingSubmissions()
     */
    @Query("SELECT s FROM SpeakingSubmission s WHERE s.user.id = :userId AND s.status = :status")
    List<SpeakingSubmission> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * Get average speaking score for a user
     * Used in getUserStats()
     */
    @Query("SELECT AVG(s.overallScore) FROM SpeakingSubmission s WHERE s.user.id = :userId AND s.status = 'completed' AND s.overallScore IS NOT NULL")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
}