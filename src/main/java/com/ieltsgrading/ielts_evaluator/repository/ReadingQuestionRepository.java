package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.ReadingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingQuestionRepository extends JpaRepository<ReadingQuestion, Integer> {
    @Query("SELECT q FROM ReadingQuestion q " +
            "JOIN q.group g " +
            "JOIN g.passage p " +
            "WHERE p.test.id = :testId") // Traverse Q -> Group -> Passage -> Test ID
    List<ReadingQuestion> findAllByTestId(@Param("testId") int testId);

    // Also, ensure you have the findAllById for the optimization:
    List<ReadingQuestion> findAllById(Iterable<Integer> ids);
}