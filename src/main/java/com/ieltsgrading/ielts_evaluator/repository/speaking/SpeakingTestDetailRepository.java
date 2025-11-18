package com.ieltsgrading.ielts_evaluator.repository.speaking;

import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTest;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeakingTestDetailRepository extends JpaRepository<SpeakingTestDetail, Integer> {
    // Custom query methods can be added here, e.g., 
    // List<SpeakingTest> findByTestDate(String testDate);
}