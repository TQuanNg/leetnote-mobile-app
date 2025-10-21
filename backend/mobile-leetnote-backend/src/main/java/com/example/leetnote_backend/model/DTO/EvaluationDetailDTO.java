package com.example.leetnote_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDetailDTO {
    private Long evaluationId;
    private Long problemId;
    private String problemTitle;
    private String difficulty;
    private LocalDateTime createdAt;
    private EvaluationDTO evaluation;
    private String solutionText;
}
