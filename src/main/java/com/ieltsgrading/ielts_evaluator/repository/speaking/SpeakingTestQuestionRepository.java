package com.ieltsgrading.ielts_evaluator.repository.speaking;

import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTest;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpeakingTestQuestionRepository extends JpaRepository<SpeakingTestQuestion, Integer> {    // Custom query methods can be added here, e.g.,
    // List<SpeakingTest> findByTestDate(String testDate);
}