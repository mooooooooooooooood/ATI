package com.ieltsgrading.ielts_evaluator.repository.speaking;

import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingSubmissionDetail;
import com.ieltsgrading.ielts_evaluator.model.SpeakingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ✅ Repository for SpeakingSubmissionDetail
 */
@Repository
public interface SpeakingSubmissionDetailRepository extends JpaRepository<SpeakingSubmissionDetail, Long> {
    
    /**
     * Find detail by submission
     */
    Optional<SpeakingSubmissionDetail> findBySubmission(SpeakingSubmission submission);
    
    /**
     * Find detail by submission ID
     */
    Optional<SpeakingSubmissionDetail> findBySubmission_SubmissionId(Long submissionId);
}