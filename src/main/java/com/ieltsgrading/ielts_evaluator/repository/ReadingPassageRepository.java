package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.ReadingPassage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingPassageRepository extends JpaRepository<ReadingPassage, Integer> {
    // FIX: Queries through the 'test' field of ReadingPassage to find the 'id' field of ReadingTest
    List<ReadingPassage> findByTest_Id(int testId);
}