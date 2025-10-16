package com.example.leetnote_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProblemDetailDTO {
    private Long id;
    private String title;
    String difficulty;
    String description;
    boolean isFavorite;
    boolean solved;
    private SolutionDTO solution;
}
