package com.ieltsgrading.ielts_evaluator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ieltsgrading.ielts_evaluator.model.IeltsSpeakingTest;

public interface IeltsSpeakingTestRepository extends JpaRepository<IeltsSpeakingTest, Long> {
}
