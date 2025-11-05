package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.ReadingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingQuestionRepository extends JpaRepository<ReadingQuestion, Integer> {
    // FIX: Queries through the 'group' field of R.Question to find the 'id' field of ReadingQuestionGroup
    List<ReadingQuestion> findByGroup_Id(int groupId);
}