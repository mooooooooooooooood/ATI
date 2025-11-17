package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeakingSubmissionRepository extends JpaRepository<SpeakingSubmission, Long> {
    
    Optional<SpeakingSubmission> findBySubmissionUuid(String submissionUuid);
    
    List<SpeakingSubmission> findByUserOrderBySubmittedAtDesc(User user);
    
    long countByUser(User user);
    
    long countByUserAndStatus(User user, String status);
    
    @Query("SELECT AVG(s.overallScore) FROM SpeakingSubmission s WHERE s.user.id = :userId AND s.status = 'completed'")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
}
