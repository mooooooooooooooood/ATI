package com.ieltsgrading.ielts_evaluator.repository.listening;

import com.ieltsgrading.ielts_evaluator.model.listening.ListeningSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningSectionRepository extends JpaRepository<ListeningSection, Integer> {

    /**
     * Custom method example: Finds all sections belonging to a specific test, ordered correctly.
     * @param testId The ID of the parent test.
     * @return A list of sections ordered by section_order.
     */
    List<ListeningSection> findAllByTest_TestIdOrderBySectionOrderAsc(Integer testId);
}