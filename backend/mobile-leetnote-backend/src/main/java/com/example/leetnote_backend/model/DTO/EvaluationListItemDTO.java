package com.example.leetnote_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationListItemDTO {
    private Long evaluationId;
    private Long problemId;
    private String problemTitle;
    private LocalDateTime createdAt;
}
