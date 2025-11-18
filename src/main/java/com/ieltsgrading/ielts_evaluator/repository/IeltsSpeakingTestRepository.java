package com.ieltsgrading.ielts_evaluator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ieltsgrading.ielts_evaluator.model.IeltsSpeakingTest;

import java.util.Optional;

@Repository
public interface IeltsSpeakingTestRepository extends JpaRepository<IeltsSpeakingTest, Long> {
    
    // Find by test_id (Long)
    Optional<IeltsSpeakingTest> findById(Long id);
    
    // Search by test date or main topic
    @Query("SELECT t FROM IeltsSpeakingTest t WHERE " +
           "LOWER(t.testDate) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.mainTopic) LIKE LOWER(CONCAT('%', :query, '%'))")
    java.util.List<IeltsSpeakingTest> searchTests(@Param("query") String query);
    
    // Count total attempts (mock for now)
    @Query("SELECT COUNT(t) FROM IeltsSpeakingTest t")
    long countAttempts();
}