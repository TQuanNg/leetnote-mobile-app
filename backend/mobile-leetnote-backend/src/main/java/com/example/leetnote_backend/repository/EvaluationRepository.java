package com.example.leetnote_backend.repository;

import com.example.leetnote_backend.model.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findTopBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);
    List<Evaluation> findBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);
}
