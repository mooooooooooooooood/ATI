package com.ieltsgrading.ielts_evaluator; // Check the package name!

import com.ieltsgrading.ielts_evaluator.model.ReadingTest;
import com.ieltsgrading.ielts_evaluator.repository.ReadingTestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // New Import
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace; // New Import
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// ADD THIS LINE: Tells Spring NOT to replace your external MySQL database
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ReadingTestRepositoryTest {

    @Autowired
    private ReadingTestRepository testRepository;

    @Test
    void shouldFindTestRecordsInDatabase() {
        // --- 1. Pull all data ---
        List<ReadingTest> tests = testRepository.findAll();

        // --- 2. Print results to console (for debugging) ---
        System.out.println("--- START OF DEBUG OUTPUT ---");
        System.out.println("Total tests found: " + tests.size());

        if (tests.isEmpty()) {
            System.out.println("Status: FAILED. No records found. Data must be inserted or connection is wrong.");
        } else {
            tests.forEach(test -> {
                System.out.println("Status: SUCCESS! ID: " + test.getId() + ", Name: " + test.getTestName());
            });
        }
        System.out.println("--- END OF DEBUG OUTPUT ---");

        // --- 3. Assertion ---
        assertThat(tests).as("Check if ReadingTest table contains records").isNotEmpty();
    }
}