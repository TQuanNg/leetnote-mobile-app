package com.example.leetnote_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SolutionDTO {
    private String approach;
    private String code;
    private String timeComplexity;
    private String spaceComplexity;
}
