package com.ieltsgrading.ielts_evaluator.repository.listening;

import com.ieltsgrading.ielts_evaluator.model.listening.ListeningQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningQuestionRepository extends JpaRepository<ListeningQuestion, Integer> {

    /**
     * FIX: Removed the redundant 'Question' property from the method name.
     * The path now starts with 'Group' which is a property in ListeningQuestion.
     * Path: ListeningQuestion -> Group -> Section -> Test -> TestId
     */
    List<ListeningQuestion> findAllByGroup_Section_Test_TestId(Integer testId);
}