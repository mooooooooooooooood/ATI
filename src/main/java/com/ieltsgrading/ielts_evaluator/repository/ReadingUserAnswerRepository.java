// ReadingUserAnswerRepository.java
package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.ReadingUserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingUserAnswerRepository extends JpaRepository<ReadingUserAnswer, Integer> {
    // Basic CRUD operations are inherited
}