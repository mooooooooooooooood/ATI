package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, Long> {
    
    Optional<WritingSubmission> findBySubmissionUuid(String submissionUuid);
    
    List<WritingSubmission> findByUserOrderBySubmittedAtDesc(User user);
    
    @Query("SELECT w FROM WritingSubmission w WHERE w.user.id = :userId AND w.status = :status")
    List<WritingSubmission> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    long countByUser(User user);
    
    long countByUserAndStatus(User user, String status);
    
    @Query("SELECT AVG(w.overallScore) FROM WritingSubmission w WHERE w.user.id = :userId AND w.status = 'completed'")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
}
