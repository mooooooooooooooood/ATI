package com.ieltsgrading.ielts_evaluator.hook;

import com.ieltsgrading.ielts_evaluator.model.IeltsSpeakingTest;
import com.ieltsgrading.ielts_evaluator.repository.IeltsSpeakingTestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private IeltsSpeakingTestRepository speakingTestRepository; // your JPA repository

    @Override
    public void run(String... args) throws Exception {
        // Check if the table is empty
        if (speakingTestRepository.count() == 0) {
            System.out.println("⚙️ No IELTS tests found — inserting default data...");

            IeltsSpeakingTest test = new IeltsSpeakingTest();
            test.setPart1Question("Tell me about your hometown.");
            test.setPart2Question("Describe a memorable journey you have taken.");
            test.setPart3Question("What are the advantages of travelling?");
            speakingTestRepository.save(test);

            System.out.println("✅ Default IELTS test inserted!");
        } else {
            System.out.println("✅ IELTS test table already has data — skipping initialization.");
        }
    }
}
