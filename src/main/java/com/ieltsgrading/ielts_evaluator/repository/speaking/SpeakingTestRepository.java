package com.ieltsgrading.ielts_evaluator.repository.speaking;

import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpeakingTestRepository extends JpaRepository<SpeakingTest, Integer> {
    // Custom query methods can be added here, e.g., 
    // List<SpeakingTest> findByTestDate(String testDate);
    List<SpeakingTest> findByMainTopicContainingIgnoreCase(String keyword);
}