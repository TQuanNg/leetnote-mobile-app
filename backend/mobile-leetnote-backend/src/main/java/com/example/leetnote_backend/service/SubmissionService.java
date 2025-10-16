package com.example.leetnote_backend.service;


import com.example.leetnote_backend.model.entity.Submission;
import com.example.leetnote_backend.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public Optional<Submission> getLastSubmission(Long userId, Long problemId) {
        return submissionRepository.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId);
    }
}
