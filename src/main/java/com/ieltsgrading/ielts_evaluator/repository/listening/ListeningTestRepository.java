package com.ieltsgrading.ielts_evaluator.repository.listening;

import com.ieltsgrading.ielts_evaluator.model.listening.ListeningTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListeningTestRepository extends JpaRepository<ListeningTest, Integer> {
    // Spring Data JPA provides CRUD methods automatically.

    /**
     * Custom method example: Finds a test by its specific name.
     * @param testName The name of the test (e.g., "Cambridge 20 - Test 1").
     * @return The found ListeningTest entity.
     */
    ListeningTest findByTestName(String testName);
    @Query("SELECT t FROM ListeningTest t " +
            "LEFT JOIN FETCH t.sections s " +
            "LEFT JOIN FETCH s.questionGroups g " +
            "LEFT JOIN FETCH g.questions q " +
            "WHERE t.testId = :testId")
    Optional<ListeningTest> findByIdWithDetails(@Param("testId") Integer testId);
}