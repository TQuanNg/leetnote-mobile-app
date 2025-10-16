package com.example.leetnote_backend.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubmissionRequest {
    private Long userId;      // optional if you get from auth token
    private Long problemId;
    private String solutionText;
}
