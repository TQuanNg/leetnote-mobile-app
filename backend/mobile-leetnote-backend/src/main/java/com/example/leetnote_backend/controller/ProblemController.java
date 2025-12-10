package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.ProblemDetailDTO;
import com.example.leetnote_backend.model.DTO.ProblemListDTO;
import com.example.leetnote_backend.service.ProblemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public Page<ProblemListDTO> getAllProblems(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> difficulties,
            @RequestParam(required = false) Boolean isSolved,
            @RequestParam(required = false) Boolean isFavorite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userPrincipal.getUserId();
        Pageable pageable = PageRequest.of(page, size);
        return problemService.getAllProblems(userId, keyword, difficulties, isSolved, isFavorite, pageable);
    }

    @GetMapping("/detail")
    public ResponseEntity<ProblemDetailDTO> getProblemById(
            @RequestParam Long problemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();

        ProblemDetailDTO problemDetailDTO = problemService.getProblemDetail(problemId, userId);
        if (problemDetailDTO == null) {
            throw new RuntimeException("Problem not found with id: " + problemId);
        }
        return ResponseEntity.ok(problemDetailDTO);
    }

    @PutMapping("/{problemId}/status")
    public ResponseEntity<ProblemListDTO> updateStatus(
            @PathVariable Long problemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam boolean isSolved,
            @RequestParam boolean isFavorite
    ) {
        Long userId = userPrincipal.getUserId();
        ProblemListDTO updated = problemService.updateProblemStatus(userId, problemId, isSolved, isFavorite);
        return ResponseEntity.ok(updated);
    }
}