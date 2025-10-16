package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.ProblemDetailDTO;
import com.example.leetnote_backend.model.DTO.ProblemListDTO;
import com.example.leetnote_backend.model.entity.Problem;
import com.example.leetnote_backend.model.entity.UserProblemStatus;
import com.example.leetnote_backend.repository.ProblemRepository;
import com.example.leetnote_backend.repository.UserProblemStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProblemServiceTest {
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private UserProblemStatusRepository userProblemStatusRepository;
    @InjectMocks
    private ProblemService problemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getAllProblems returns paged DTOs with user status")
    void getAllProblems_basic() {
        Long userId = 1L;
        Problem problem = new Problem();
        problem.setId(10L);
        problem.setTitle("Two Sum");
        problem.setDifficulty("Easy");
        Page<Problem> page = new PageImpl<>(List.of(problem));

        when(problemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        UserProblemStatus status = new UserProblemStatus(userId, 10L, false, true);
        when(userProblemStatusRepository.findAllByUserId(userId)).thenReturn(List.of(status));

        Page<ProblemListDTO> result = problemService.getAllProblems(userId, null, null, null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);

        ProblemListDTO dto = result.getContent().get(0);
        assertThat(dto.getProblemId()).isEqualTo(10L);
        assertThat(dto.isFavorite()).isTrue();
        assertThat(dto.isSolved()).isFalse();
    }

    @Test
    @DisplayName("getProblemDetail returns detail with solution and user status")
    void getProblemDetail_withSolutionAndStatus() {
        Long userId = 1L, problemId = 2L;
        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setTitle("Add Two Numbers");
        problem.setDifficulty("Medium");
        problem.setDescription("desc");

        Map<String, Object> solution = new HashMap<>();
        solution.put("approach", "Greedy");
        solution.put("code", "code");
        solution.put("time_complexity", "O(n)");
        solution.put("space_complexity", "O(1)");

        problem.setSolution(solution);

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        UserProblemStatus status = new UserProblemStatus(userId, problemId, true, true);
        when(userProblemStatusRepository.findByUserIdAndProblemId(userId, problemId)).thenReturn(Optional.of(status));

        ProblemDetailDTO dto = problemService.getProblemDetail(problemId, userId);
        assertThat(dto.getId()).isEqualTo(problemId);
        assertThat(dto.isFavorite()).isTrue();
        assertThat(dto.isSolved()).isTrue();
        assertThat(dto.getSolution()).isNotNull();
        assertThat(dto.getSolution().getApproach()).isEqualTo("Greedy");
    }

    @Test
    @DisplayName("getProblemDetail throws if problem not found")
    void getProblemDetail_problemNotFound() {
        when(problemRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> problemService.getProblemDetail(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Problem not found");
    }

    @Test
    @DisplayName("updateProblemStatus creates new status if not exists")
    void updateProblemStatus_createsNew() {
        Long userId = 1L, problemId = 2L;
        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setTitle("Test");
        problem.setDifficulty("Easy");

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        when(userProblemStatusRepository.findByUserIdAndProblemId(userId, problemId)).thenReturn(Optional.empty());
        when(userProblemStatusRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProblemListDTO dto = problemService.updateProblemStatus(userId, problemId, true, false);
        assertThat(dto.getProblemId()).isEqualTo(problemId);
        assertThat(dto.isSolved()).isTrue();
        assertThat(dto.isFavorite()).isFalse();
    }

    @Test
    @DisplayName("updateProblemStatus updates existing status")
    void updateProblemStatus_updatesExisting() {
        Long userId = 1L, problemId = 2L;
        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setTitle("Test");
        problem.setDifficulty("Easy");

        UserProblemStatus status = new UserProblemStatus(userId, problemId, false, false);

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        when(userProblemStatusRepository.findByUserIdAndProblemId(userId, problemId)).thenReturn(Optional.of(status));
        when(userProblemStatusRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProblemListDTO dto = problemService.updateProblemStatus(userId, problemId, true, true);
        assertThat(dto.isSolved()).isTrue();
        assertThat(dto.isFavorite()).isTrue();
    }

    @Test
    @DisplayName("updateProblemStatus throws if problem not found")
    void updateProblemStatus_problemNotFound() {
        when(problemRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> problemService.updateProblemStatus(1L, 2L, true, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Problem not found");
    }
}
