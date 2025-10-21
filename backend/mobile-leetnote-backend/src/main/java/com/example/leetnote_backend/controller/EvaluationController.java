package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.EvaluationDTO;
import com.example.leetnote_backend.model.DTO.EvaluationListItemDTO;
import com.example.leetnote_backend.model.DTO.EvaluationDetailDTO;
import com.example.leetnote_backend.model.DTO.SubmissionRequest;
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
    public ResponseEntity<EvaluationDetailDTO> getLastEvaluation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long evaluationId,
            @RequestParam(required = false) Long problemId
    ) {
        Long userId = userPrincipal.getUserId();

        if (evaluationId != null) {
            return submissionService.getEvaluationDetailById(userId, evaluationId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        if (problemId != null) {
            return submissionService.getLastEvaluationDetail(userId, problemId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/new")
    public ResponseEntity<Evaluation> getNewEvaluation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long problemId
    ) {
        Long userId = userPrincipal.getUserId();
        return submissionService.getLastEvaluation(userId, problemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<EvaluationListItemDTO>> getAllEvaluations(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();
        List<EvaluationListItemDTO> evaluations = submissionService.getAllEvaluations(userId);
        return ResponseEntity.ok(evaluations);
    }
}
