package com.ieltsgrading.ielts_evaluator.repository.reading;

import com.ieltsgrading.ielts_evaluator.model.reading.ReadingTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingTestRepository extends JpaRepository<ReadingTest, Integer> {
    // --- You must add this method ---
    // It overrides the default behavior and ensures related collections are fetched.
    @Query("SELECT t FROM ReadingTest t LEFT JOIN FETCH t.passages p LEFT JOIN FETCH p.questionGroups g LEFT JOIN FETCH g.questions WHERE t.id = :id")
    Optional<ReadingTest> findByIdWithDetails(@Param("id") int id);
}
