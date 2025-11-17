package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListeningSubmissionRepository extends JpaRepository<ListeningSubmission, Long> {
    
    Optional<ListeningSubmission> findBySubmissionUuid(String submissionUuid);
    
    List<ListeningSubmission> findByUserOrderBySubmittedAtDesc(User user);
    
    long countByUser(User user);
    
    long countByUserAndStatus(User user, String status);
    
    @Query("SELECT AVG(l.overallScore) FROM ListeningSubmission l WHERE l.user.id = :userId AND l.status = 'completed'")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
}
