package com.ieltsgrading.ielts_evaluator.repository.listening;

import com.ieltsgrading.ielts_evaluator.model.listening.ListeningUserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningUserAnswerRepository extends JpaRepository<ListeningUserAnswer, Integer> {

    /**
     * Finds all answers submitted by a specific user for a specific test.
     * Uses JPA method naming convention to traverse relationships.
     * @param userId The ID of the user.
     * @param testId The ID of the test taken (retrieved via Question -> Group -> Section -> Test).
     * @return A list of user answers.
     */
    List<ListeningUserAnswer> findAllByUserIdAndQuestion_Group_Section_Test_TestId(Integer userId, Integer testId);

    // --- Method 1: Get ALL Answers for a Test (Used in Service for setup) ---
    /**
     * Finds all user answers (regardless of user ID) belonging to a specific test.
     * @param testId The ID of the test.
     * @return A list of all user answers submitted for that test.
     */
    List<ListeningUserAnswer> findAllByQuestion_Group_Section_Test_TestId(int testId);

    // --- Method 2: Get ALL Incorrect Answers for a Test (Crucial for Gemini Review) ---
    /**
     * Finds all INCORRECT user answers submitted by a specific user for a given test.
     * This is typically used to generate the review prompt for the AI model.
     * * NOTE: This method is a combination of the first method + a filter on 'isCorrect'.
     * * @param userId The ID of the user.
     * @param testId The ID of the test.
     * @return A list of incorrect answers submitted by the user for the test.
     */
    List<ListeningUserAnswer> findAllByUserIdAndQuestion_Group_Section_Test_TestIdAndIsCorrect(Integer userId, Integer testId, boolean isCorrect);

    // --- Method 3: Get ONLY the LATEST Submission (Optional, but often useful) ---
    /**
     * Finds the latest submission for a specific test by a specific user.
     * @param userId The ID of the user.
     * @param testId The ID of the test.
     * @return The list of answers from the latest submission for that test.
     */
    List<ListeningUserAnswer> findTop40ByUserIdAndQuestion_Group_Section_Test_TestIdOrderBySubmittedAtDesc(Integer userId, Integer testId);
}