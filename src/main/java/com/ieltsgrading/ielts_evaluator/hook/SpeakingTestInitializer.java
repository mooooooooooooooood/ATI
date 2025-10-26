package com.ieltsgrading.ielts_evaluator.hook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.ieltsgrading.ielts_evaluator.model.IeltsSpeakingTest;
import com.ieltsgrading.ielts_evaluator.repository.IeltsSpeakingTestRepository;

@Component
public class SpeakingTestInitializer implements CommandLineRunner {

    @Autowired
    private IeltsSpeakingTestRepository testRepository;

    @Override
    public void run(String... args) {
        if (testRepository.count() == 0) {
            System.out.println("⚙️ IELTS Speaking Test table is empty — inserting default tests...");

            IeltsSpeakingTest test1 = new IeltsSpeakingTest();
            test1.setTestTitle("IELTS Speaking Test 1");
            test1.setPart1Question("What do you do in your free time?");
            test1.setPart2Question("Describe a hobby you enjoy.");
            test1.setPart3Question("Why do people need hobbies?");

            IeltsSpeakingTest test2 = new IeltsSpeakingTest();
            test2.setTestTitle("IELTS Speaking Test 2");
            test2.setPart1Question("Do you like reading books?");
            test2.setPart2Question("Describe a book that left a strong impression on you.");
            test2.setPart3Question("How can reading influence a person’s life?");

            IeltsSpeakingTest test3 = new IeltsSpeakingTest();
            test3.setTestTitle("IELTS Speaking Test 3");
            test3.setPart1Question("Do you like travelling?");
            test3.setPart2Question("Describe a memorable journey you have taken.");
            test3.setPart3Question("What are the benefits of travelling abroad?");

            testRepository.save(test1);
            testRepository.save(test2);
            testRepository.save(test3);

            System.out.println("✅ Default IELTS Speaking Tests inserted!");
        } else {
            System.out.println("✅ IELTS Speaking Test table already has data — skipping initialization.");
        }
    }
}
