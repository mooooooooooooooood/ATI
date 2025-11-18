package com.ieltsgrading.ielts_evaluator.repository.reading;

import com.ieltsgrading.ielts_evaluator.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingSubmissionRepository extends JpaRepository<ReadingSubmission, Long> {
    
    Optional<ReadingSubmission> findBySubmissionUuid(String submissionUuid);
    
    List<ReadingSubmission> findByUserOrderBySubmittedAtDesc(User user);
    
    long countByUser(User user);
    
    long countByUserAndStatus(User user, String status);
    
    @Query("SELECT AVG(r.overallScore) FROM ReadingSubmission r WHERE r.user.id = :userId AND r.status = 'completed'")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
}
