// ReadingUserAnswerRepository.java
package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.ReadingUserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingUserAnswerRepository extends JpaRepository<ReadingUserAnswer, Integer> {
    // Basic CRUD operations are inherited
    @Query("SELECT r FROM ReadingUserAnswer r JOIN r.question q JOIN q.group g JOIN g.passage p JOIN p.test t WHERE t.id = :testId")
    List<ReadingUserAnswer> findAllByTestId(@Param("testId") int testId);
}