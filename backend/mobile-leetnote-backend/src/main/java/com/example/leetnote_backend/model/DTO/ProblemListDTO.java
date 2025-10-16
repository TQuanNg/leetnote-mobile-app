package com.example.leetnote_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProblemListDTO {
    Long problemId;
    String title;
    String difficulty;
    boolean isFavorite;
    boolean isSolved;
}
