package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.SubmissionRequest;
import com.example.leetnote_backend.model.DTO.EvaluationDTO;
import com.example.leetnote_backend.model.DTO.EvaluationListItemDTO;
import com.example.leetnote_backend.model.entity.Evaluation;
import com.example.leetnote_backend.model.entity.Problem;
import com.example.leetnote_backend.model.entity.Submission;
import com.example.leetnote_backend.repository.EvaluationRepository;
import com.example.leetnote_backend.repository.ProblemRepository;
import com.example.leetnote_backend.repository.SubmissionRepository;
import com.example.leetnote_backend.util.PromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private EvaluationRepository evaluationRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private TogetherAiService togetherAiService;
    @Mock
    private PromptBuilder promptBuilder;

    @InjectMocks
    private EvaluationService evaluationService;

    private final Long userId = 1L;
    private final Long problemId = 42L;
    private SubmissionRequest request;

    @BeforeEach
    void setUp() {
        request = new SubmissionRequest(null, problemId, "print('hello')");
    }

    @Test
    void createSubmissionWithEvaluation_success_withProblemDescription() {
        // Arrange
        Submission savedSubmission = new Submission();
        savedSubmission.setId(10L);
        savedSubmission.setUserId(userId);
        savedSubmission.setProblemId(problemId);
        savedSubmission.setSolutionText(request.getSolutionText());
        savedSubmission.setCreatedAt(LocalDateTime.now());

        when(submissionRepository.save(any(Submission.class)))
                .thenReturn(savedSubmission);

        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setDescription("Problem description text");
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));

        when(promptBuilder.buildPrompt(eq("Problem description text"), eq(request.getSolutionText())))
                .thenReturn("PROMPT");

        when(togetherAiService.callTogetherModel("PROMPT")).thenReturn("{\"rating\":3,\"issue\":[\"i1\"],\"feedback\":[\"f1\"]}");
        Map<String,Object> parsed = new HashMap<>();
        parsed.put("rating", 3);
        parsed.put("issue", List.of("i1"));
        parsed.put("feedback", List.of("f1"));
        when(togetherAiService.parseResponse(anyString())).thenReturn(parsed);

        when(evaluationRepository.save(any(Evaluation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(submissionRepository.findByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId))
                .thenReturn(Arrays.asList(savedSubmission, new Submission(), new Submission()));

        // Act
        EvaluationDTO dto = evaluationService.createSubmissionWithEvaluation(userId, request);

        // Assert
        assertNotNull(dto);
        assertEquals(3, dto.getRating());
        assertThat(dto.getIssue()).containsExactly("i1");
        assertThat(dto.getFeedback()).containsExactly("f1");

        // Verify evaluation saved with version=1 and linked submission
        ArgumentCaptor<Evaluation> evalCaptor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationRepository).save(evalCaptor.capture());
        Evaluation savedEval = evalCaptor.getValue();
        assertEquals(Short.valueOf((short)1), savedEval.getVersion());
        assertNotNull(savedEval.getCreatedAt());
        assertNotNull(savedEval.getSubmission());
        assertEquals(savedSubmission.getId(), savedEval.getSubmission().getId());
        assertNotNull(savedEval.getEvaluation());
        assertEquals(3, savedEval.getEvaluation().getRating());

        verify(promptBuilder).buildPrompt("Problem description text", request.getSolutionText());
        verify(togetherAiService).callTogetherModel("PROMPT");
        verify(togetherAiService).parseResponse(anyString());

        // Since we returned 3 submissions total, no deletion should happen
        verify(submissionRepository, never()).deleteAll(anyList());
    }

    @Test
    void createSubmissionWithEvaluation_success_whenProblemMissing_usesFallback() {
        // Arrange
        Submission savedSubmission = new Submission();
        savedSubmission.setId(11L);
        savedSubmission.setUserId(userId);
        savedSubmission.setProblemId(problemId);
        savedSubmission.setSolutionText(request.getSolutionText());

        when(submissionRepository.save(any(Submission.class)))
                .thenReturn(savedSubmission);

        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());
        when(promptBuilder.buildPrompt(eq("No problem found."), eq(request.getSolutionText())))
                .thenReturn("PROMPT-FALLBACK");

        when(togetherAiService.callTogetherModel("PROMPT-FALLBACK")).thenReturn("{\"rating\":5,\"issue\":[\"ok\"],\"feedback\":[\"g\"]}");
        when(togetherAiService.parseResponse(anyString())).thenReturn(Map.of(
                "rating", 5,
                "issue", List.of("ok"),
                "feedback", List.of("g")
        ));

        when(evaluationRepository.save(any(Evaluation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(submissionRepository.findByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId))
                .thenReturn(Collections.singletonList(savedSubmission));

        // Act
        EvaluationDTO dto = evaluationService.createSubmissionWithEvaluation(userId, request);

        // Assert
        assertEquals(5, dto.getRating());
        verify(promptBuilder).buildPrompt("No problem found.", request.getSolutionText());
        verify(submissionRepository, never()).deleteAll(anyList());
    }

    @Test
    void createSubmissionWithEvaluation_deletesOldSubmissionsBeyond3() {
        // Arrange: 5 existing submissions => expect delete of last 2
        Submission s1 = submission(101L);
        Submission s2 = submission(102L);
        Submission s3 = submission(103L);
        Submission s4 = submission(104L);
        Submission s5 = submission(105L);
        List<Submission> existing = Arrays.asList(s1, s2, s3, s4, s5);

        when(submissionRepository.save(any(Submission.class)))
                .thenReturn(submission(200L));

        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());
        when(promptBuilder.buildPrompt(anyString(), anyString())).thenReturn("P");
        when(togetherAiService.callTogetherModel(anyString())).thenReturn("{}");
        when(togetherAiService.parseResponse(anyString())).thenReturn(Map.of(
                "rating", 2,
                "issue", List.of("x"),
                "feedback", List.of("y")
        ));
        when(evaluationRepository.save(any(Evaluation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(submissionRepository.findByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId))
                .thenReturn(existing);

        // Act
        evaluationService.createSubmissionWithEvaluation(userId, request);

        // Assert: capture deletions
        ArgumentCaptor<List<Submission>> toDeleteCaptor = ArgumentCaptor.forClass((Class<List<Submission>>) (Class<?>) List.class);
        verify(submissionRepository).deleteAll(toDeleteCaptor.capture());
        List<Submission> toDelete = toDeleteCaptor.getValue();
        assertThat(toDelete).extracting(Submission::getId).containsExactly(104L, 105L);
    }

    @Test
    void createSubmissionWithEvaluation_connectionError_wrapped() {
        when(submissionRepository.save(any(Submission.class)))
                .thenReturn(submission(210L));
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());
        when(promptBuilder.buildPrompt(anyString(), anyString())).thenReturn("PROMPT");
        when(togetherAiService.callTogetherModel("PROMPT"))
                .thenThrow(new ResourceAccessException("connect timeout"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evaluationService.createSubmissionWithEvaluation(userId, request));
        assertTrue(ex.getMessage().contains("Failed to connect to the evaluation service"));

        // No cleanup should be attempted if upstream call failed before deletion
        verify(submissionRepository, never()).deleteAll(anyList());
    }

    @Test
    void createSubmissionWithEvaluation_http4xx_wrapped() {
        when(submissionRepository.save(any(Submission.class)))
                .thenReturn(submission(220L));
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());
        when(promptBuilder.buildPrompt(anyString(), anyString())).thenReturn("PROMPT");
        when(togetherAiService.callTogetherModel("PROMPT"))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evaluationService.createSubmissionWithEvaluation(userId, request));
        assertTrue(ex.getMessage().contains("Evaluation service error: 400 BAD_REQUEST"));
    }

    @Test
    void createSubmissionWithEvaluation_http5xx_wrapped() {
        when(submissionRepository.save(any(Submission.class)))
                .thenReturn(submission(230L));
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());
        when(promptBuilder.buildPrompt(anyString(), anyString())).thenReturn("PROMPT");
        when(togetherAiService.callTogetherModel("PROMPT"))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evaluationService.createSubmissionWithEvaluation(userId, request));
        assertTrue(ex.getMessage().contains("Evaluation service error: 500 INTERNAL_SERVER_ERROR"));
    }

    @Test
    void getLastEvaluationDetail_returnsDetail() {
        // Arrange
        Long evalId = 777L;
        Submission sub = submission(300L);
        sub.setSolutionText("solution");

        Evaluation ev = new Evaluation();
        ev.setId(evalId);
        ev.setSubmission(sub);
        ev.setCreatedAt(LocalDateTime.now());
        EvaluationDTO edto = new EvaluationDTO();
        edto.setRating(4);
        ev.setEvaluation(edto);

        Problem p = new Problem();
        p.setId(problemId);
        p.setTitle("Two Sum");
        p.setDifficulty("Easy");

        when(evaluationRepository.findTopBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(userId, problemId))
                .thenReturn(Optional.of(ev));
        when(evaluationRepository.findById(evalId)).thenReturn(Optional.of(ev));
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(p));

        // Act
        var result = evaluationService.getLastEvaluationDetail(userId, problemId);

        // Assert
        assertTrue(result.isPresent());
        var detail = result.get();
        assertEquals(evalId, detail.getEvaluationId());
        assertEquals(problemId, detail.getProblemId());
        assertEquals("Two Sum", detail.getProblemTitle());
        assertEquals("Easy", detail.getDifficulty());
        assertEquals(4, detail.getEvaluation().getRating());
        assertEquals("solution", detail.getSolutionText());

        verify(evaluationRepository).findTopBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(userId, problemId);
        verify(evaluationRepository).findById(evalId);
        verify(problemRepository).findById(problemId);
    }

    @Test
    void getAllEvaluations_returnsSingleLatestItem_withProblemTitleAndCreatedAt() {
        // Arrange
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(problemId);

        EvaluationDTO evalDto1 = new EvaluationDTO();
        evalDto1.setRating(4);
        Evaluation e1 = new Evaluation();
        e1.setId(1L);
        e1.setSubmission(submission);
        e1.setEvaluation(evalDto1);
        e1.setCreatedAt(LocalDateTime.now());

        // Mock the correct method that getAllEvaluations actually calls
        when(evaluationRepository.findLatestEvaluationsByUserId(userId))
                .thenReturn(List.of(e1));

        Problem p = new Problem();
        p.setId(problemId);
        p.setTitle("Two Sum");
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(p));

        // Act
        List<EvaluationListItemDTO> result = evaluationService.getAllEvaluations(userId);

        // Assert
        assertEquals(1, result.size());
        EvaluationListItemDTO item = result.getFirst();
        assertEquals(1L, item.getEvaluationId());
        assertEquals(problemId, item.getProblemId());
        assertEquals("Two Sum", item.getProblemTitle());
        assertEquals(e1.getCreatedAt(), item.getCreatedAt());

        verify(evaluationRepository).findLatestEvaluationsByUserId(userId);
        verify(problemRepository).findById(problemId);
    }

    @Test
    void getAllEvaluations_returnsEmptyList_whenNoEvaluations() {
        // Mock the correct method
        when(evaluationRepository.findLatestEvaluationsByUserId(userId))
                .thenReturn(Collections.emptyList());

        List<EvaluationListItemDTO> result = evaluationService.getAllEvaluations(userId);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(evaluationRepository).findLatestEvaluationsByUserId(userId);
        // No need to verify problemRepository since no evaluations exist
    }

    private Submission submission(Long id) {
        Submission s = new Submission();
        s.setId(id);
        s.setUserId(userId);
        s.setProblemId(problemId);
        s.setSolutionText("sol");
        s.setCreatedAt(LocalDateTime.now());
        return s;
    }
}
