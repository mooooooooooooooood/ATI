package com.ieltsgrading.ielts_evaluator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ieltsgrading.ielts_evaluator.repository.IeltsSpeakingTestRepository;
import java.util.List;
import com.ieltsgrading.ielts_evaluator.model.IeltsSpeakingTest;

@RestController
public class SpeakingTestController {

    @Autowired
    private IeltsSpeakingTestRepository repo;

    @GetMapping("/speaking-tests")
    public List<IeltsSpeakingTest> getAllTests() {
        return repo.findAll();
    }
}
