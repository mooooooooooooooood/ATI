package com.ieltsgrading.ielts_evaluator.repository.listening;

import com.ieltsgrading.ielts_evaluator.model.listening.ListeningQuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListeningQuestionGroupRepository extends JpaRepository<ListeningQuestionGroup, Integer> {

    // Custom query methods can be added here if needed for direct group lookups.
}