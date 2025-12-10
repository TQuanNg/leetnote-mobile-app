package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.entity.Submission;
import com.example.leetnote_backend.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<Submission> getLastSubmission(
            @PathVariable Long problemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();

        return submissionService.getLastSubmission(userId, problemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
