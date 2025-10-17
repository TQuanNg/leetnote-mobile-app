package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.entity.Submission;
import com.example.leetnote_backend.repository.SubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void getLastSubmission_returnsLatest_whenExists() {
        Long userId = 1L;
        Long problemId = 99L;
        Submission latest = new Submission();
        latest.setId(2L);
        latest.setUserId(userId);
        latest.setProblemId(problemId);
        latest.setSolutionText("code v2");
        latest.setCreatedAt(LocalDateTime.now());

        when(submissionRepository.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId))
                .thenReturn(Optional.of(latest));

        Optional<Submission> result = submissionService.getLastSubmission(userId, problemId);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId());
        verify(submissionRepository, times(1))
                .findTopByUserIdAndProblemIdOrderByCreatedAtDesc(eq(userId), eq(problemId));
        verifyNoMoreInteractions(submissionRepository);
    }

    @Test
    void getLastSubmission_returnsEmpty_whenNone() {
        Long userId = 2L;
        Long problemId = 100L;
        when(submissionRepository.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId))
                .thenReturn(Optional.empty());

        Optional<Submission> result = submissionService.getLastSubmission(userId, problemId);

        assertTrue(result.isEmpty());
        verify(submissionRepository).findTopByUserIdAndProblemIdOrderByCreatedAtDesc(eq(userId), eq(problemId));
        verifyNoMoreInteractions(submissionRepository);
    }

    @Test
    void getLastSubmission_propagatesRepositoryException() {
        Long userId = 3L;
        Long problemId = 101L;
        when(submissionRepository.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId))
                .thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> submissionService.getLastSubmission(userId, problemId));
        assertTrue(ex.getMessage().contains("DB error"));
        verify(submissionRepository).findTopByUserIdAndProblemIdOrderByCreatedAtDesc(eq(userId), eq(problemId));
        verifyNoMoreInteractions(submissionRepository);
    }
}
