package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.EvaluationDTO;
import com.example.leetnote_backend.model.DTO.EvaluationDetailDTO;
import com.example.leetnote_backend.model.DTO.EvaluationListItemDTO;
import com.example.leetnote_backend.model.DTO.SubmissionRequest;
import com.example.leetnote_backend.model.entity.Evaluation;
import com.example.leetnote_backend.model.entity.Problem;
import com.example.leetnote_backend.model.entity.Submission;
import com.example.leetnote_backend.repository.EvaluationRepository;
import com.example.leetnote_backend.repository.ProblemRepository;
import com.example.leetnote_backend.repository.SubmissionRepository;
import com.example.leetnote_backend.util.PromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final ProblemRepository problemRepository;
    private final TogetherAiService togetherAiService;
    private final PromptBuilder promptBuilder;

    @Transactional
    public EvaluationDTO createSubmissionWithEvaluation(
            Long userId,
            SubmissionRequest req) {

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(req.getProblemId());
        submission.setSolutionText(req.getSolutionText());
        submission = submissionRepository.save(submission);

        try {
            String problemText = problemRepository.findById(req.getProblemId())
                    .map(Problem::getDescription)
                    .orElse("No problem found.");

            String prompt = promptBuilder.buildPrompt(problemText, req.getSolutionText());
            String rawResponse = togetherAiService.callTogetherModel(prompt);
            Map<String, Object> parsed = togetherAiService.parseResponse(rawResponse);

            EvaluationDTO evaluationDto = new ObjectMapper().convertValue(parsed, EvaluationDTO.class);

            Evaluation evaluation = new Evaluation();
            evaluation.setSubmission(submission);
            evaluation.setVersion((short) 1); // free tier = version 1
            evaluation.setEvaluation(evaluationDto);
            evaluation.setCreatedAt(LocalDateTime.now());
            evaluation = evaluationRepository.save(evaluation);

            deleteOldSubmissions(userId, req.getProblemId());

            return evaluationDto;
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Failed to connect to the evaluation service", e);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new RuntimeException("Evaluation service error: " + ex.getStatusCode());
        }
    }

    private void deleteOldSubmissions(Long userId, Long problemId) {
        // Fetch submissions for this user/problem ordered by creation time descending
        List<Submission> submissions = submissionRepository
                .findByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId);

        // Keep only the 3 most recent
        if (submissions.size() > 3) {
            List<Submission> toDelete = submissions.subList(3, submissions.size());
            submissionRepository.deleteAll(toDelete);
        }
    }

    public Optional<Evaluation> getLastEvaluation(Long userId, Long problemId) {
        return evaluationRepository.findTopBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(userId, problemId);
    }

    public List<EvaluationListItemDTO> getAllEvaluations(Long userId) {
        // Get latest evaluation for each problem the user has submitted
        List<Evaluation> evaluations = evaluationRepository.findLatestEvaluationsByUserId(userId);

        return evaluations.stream()
                .map(evaluation -> {
                    Long problemId = evaluation.getSubmission().getProblemId();
                    String problemTitle = problemRepository.findById(problemId)
                            .map(Problem::getTitle)
                            .orElse("Unknown Problem");

                    return new EvaluationListItemDTO(
                            evaluation.getId(),
                            problemId,
                            problemTitle,
                            evaluation.getCreatedAt()
                    );
                })
                .toList();
    }

    public Optional<EvaluationDetailDTO> getEvaluationDetailById(Long userId, Long evaluationId) {
        // When user clicks on a specific evaluation, return that eval detail and its problem info
        return evaluationRepository.findById(evaluationId)
                .filter(ev -> ev.getSubmission() != null && ev.getSubmission().getUserId().equals(userId))
                .map(ev -> {
                    Submission sub = ev.getSubmission();
                    Long pid = sub.getProblemId();
                    Problem p = problemRepository.findById(pid).orElse(null);
                    EvaluationDetailDTO dto = new EvaluationDetailDTO();
                    dto.setEvaluationId(ev.getId());
                    dto.setCreatedAt(ev.getCreatedAt());
                    dto.setEvaluation(ev.getEvaluation());
                    dto.setSolutionText(sub.getSolutionText());

                    if (p != null) {
                        dto.setProblemId(p.getId());
                        dto.setProblemTitle(p.getTitle());
                        dto.setDifficulty(p.getDifficulty());
                    } else {
                        dto.setProblemId(pid);
                        dto.setProblemTitle("Unknown Problem");
                        dto.setDifficulty(null);
                    }
                    return dto;
                });
    }

    public Optional<EvaluationDetailDTO> getLastEvaluationDetail(Long userId, Long problemId) {
        return evaluationRepository
                .findTopBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(userId, problemId)
                .flatMap(ev -> getEvaluationDetailById(userId, ev.getId()));
    }
}
