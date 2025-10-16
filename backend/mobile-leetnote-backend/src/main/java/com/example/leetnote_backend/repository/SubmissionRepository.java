package com.example.leetnote_backend.repository;

import com.example.leetnote_backend.model.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findTopByUserIdAndProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);
    List<Submission> findByUserIdAndProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);
}

