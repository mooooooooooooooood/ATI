package com.ieltsgrading.ielts_evaluator.repository;

import com.ieltsgrading.ielts_evaluator.model.IeltsWritingTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IeltsWritingTest entity
 * Provides database operations for writing tests
 */
@Repository
public interface IeltsWritingTestRepository extends JpaRepository<IeltsWritingTest, Integer> {
    
    /**
     * Find test by display ID (e.g., "cam20-test4")
     * This requires parsing the ID to get test_id
     */
    @Query("SELECT t FROM IeltsWritingTest t WHERE " +
           "t.testId = :testId")
    Optional<IeltsWritingTest> findByTestId(@Param("testId") Integer testId);
    
    /**
     * Find all tests sorted by test_id descending (newest first)
     */
    List<IeltsWritingTest> findAllByOrderByTestIdDesc();
    
    /**
     * Find all tests sorted by test_id ascending (oldest first)
     */
    List<IeltsWritingTest> findAllByOrderByTestIdAsc();
    
    /**
     * Search tests by question content
     */
    @Query("SELECT t FROM IeltsWritingTest t WHERE " +
           "LOWER(t.task1Question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.task2Question) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<IeltsWritingTest> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Find tests by CAM number
     * CAM 20 = test_id 1-4
     * CAM 19 = test_id 5-8
     * CAM 18 = test_id 9-12
     * etc.
     */
    @Query("SELECT t FROM IeltsWritingTest t WHERE " +
           "t.testId >= :startId AND t.testId <= :endId " +
           "ORDER BY t.testId ASC")
    List<IeltsWritingTest> findByCamNumber(@Param("startId") Integer startId, 
                                           @Param("endId") Integer endId);
    
    /**
     * Count total tests
     */
    @Query("SELECT COUNT(t) FROM IeltsWritingTest t")
    long countAllTests();
    
    /**
     * Find test by CAM and test number
     * Example: CAM 20, Test 4 -> test_id = 4
     */
    default Optional<IeltsWritingTest> findByCamAndTestNumber(int camNumber, int testNumber) {
        // Calculate test_id from CAM number and test number
        // CAM 20 = test_id 1-4
        // CAM 19 = test_id 5-8
        // Formula: test_id = (20 - camNumber) * 4 + testNumber
        int testId = (20 - camNumber) * 4 + testNumber;
        return findById(testId);
    }
    
    /**
     * Find test by display ID (e.g., "cam20-test4")
     */
    default Optional<IeltsWritingTest> findByDisplayId(String displayId) {
        try {
            // Parse "cam20-test4" to get camNumber=20, testNumber=4
            String[] parts = displayId.toLowerCase().split("-");
            if (parts.length != 2) return Optional.empty();
            
            int camNumber = Integer.parseInt(parts[0].replace("cam", ""));
            int testNumber = Integer.parseInt(parts[1].replace("test", ""));
            
            return findByCamAndTestNumber(camNumber, testNumber);
            
        } catch (Exception e) {
            System.err.println("Error parsing display ID: " + displayId);
            return Optional.empty();
        }
    }
    
    /**
     * Get tests for specific CAM
     */
    default List<IeltsWritingTest> findTestsByCam(int camNumber) {
        int startId = (20 - camNumber) * 4 + 1;
        int endId = startId + 3;
        return findByCamNumber(startId, endId);
    }
}