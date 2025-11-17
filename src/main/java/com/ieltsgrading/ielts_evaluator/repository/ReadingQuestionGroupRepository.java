package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.ReadingQuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingQuestionGroupRepository extends JpaRepository<ReadingQuestionGroup, Integer> {
    // FIX: Queries through the 'passage' field of R.Q.Group to find the 'passageId' field of ReadingPassage
    List<ReadingQuestionGroup> findByPassage_PassageId(int passageId);
}