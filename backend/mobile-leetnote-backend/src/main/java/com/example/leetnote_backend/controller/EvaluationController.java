package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.EvaluationDTO;
import com.example.leetnote_backend.model.entity.Evaluation;
import com.example.leetnote_backend.service.EvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/evaluations")
public class EvaluationController {

    private final EvaluationService submissionService;

    public EvaluationController(EvaluationService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<?> createSubmission(
            @RequestBody SubmissionRequest submissionRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            Long userId = userPrincipal.getUserId();
            EvaluationDTO createdEvaluation = submissionService.createSubmissionWithEvaluation(userId, submissionRequest);
            return ResponseEntity.ok(createdEvaluation);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/last")
    public ResponseEntity<Evaluation> getLastEvaluation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long problemId
    ) {
        Long userId = userPrincipal.getUserId();
        return submissionService.getLastEvaluation(userId, problemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Evaluation>> getAllEvaluations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long problemId
    ) {
        Long userId = userPrincipal.getUserId();
        List<Evaluation> evaluations = submissionService.getAllEvaluations(userId, problemId);
        return ResponseEntity.ok(evaluations);
    }
}
