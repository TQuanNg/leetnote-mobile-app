package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.ProblemDetailDTO;
import com.example.leetnote_backend.model.DTO.ProblemListDTO;
import com.example.leetnote_backend.model.DTO.SolutionDTO;
import com.example.leetnote_backend.model.entity.Problem;
import com.example.leetnote_backend.model.entity.UserProblemStatus;
import com.example.leetnote_backend.repository.ProblemRepository;
import com.example.leetnote_backend.repository.UserProblemStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.leetnote_backend.model.entity.ProblemSpecification.*;

@Service
@RequiredArgsConstructor
public class ProblemService {
    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserProblemStatusRepository userProblemStatusRepository;

    public Page<ProblemListDTO> getAllProblems(
            Long userId,
            String keyword,
            List<String> difficulties,
            Boolean isSolved,
            Boolean isFavorite,
            Pageable pageable) {
        Specification<Problem> spec = hasKeyword(keyword);

        if (difficulties != null && !difficulties.isEmpty()) {
            spec = spec.and(hasDifficulty(difficulties));
        }

        if (isSolved != null) {
            spec = spec.and(isSolved(isSolved, userId));
        }

        if (isFavorite != null) {
            spec = spec.and(isFavorited(isFavorite, userId));
        }

        Page<Problem> page = problemRepository.findAll(spec, pageable);
        List<UserProblemStatus> userProblemStatuses = userProblemStatusRepository.findAllByUserId(userId);

        Map<Long, UserProblemStatus> userProblemStatusMap = userProblemStatuses.stream()
                .collect(Collectors.toMap(UserProblemStatus::getProblemId, s -> s));

        List<ProblemListDTO> dto = page.getContent().stream()
                .map(problem -> {
                    UserProblemStatus userProblemStatus = userProblemStatusMap.get(problem.getId());
                    boolean favorited = userProblemStatus != null && userProblemStatus.isFavorited();
                    boolean solved = userProblemStatus != null && userProblemStatus.isSolved();
                    return new ProblemListDTO(
                            problem.getId(),
                            problem.getTitle(),
                            problem.getDifficulty(),
                            favorited,
                            solved
                    );
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dto, pageable, page.getTotalElements());
    }

    public ProblemDetailDTO getProblemDetail(
            Long problemId,
            Long userId
    ) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        // 2. Fetch user's problem status
        Optional<UserProblemStatus> statusOpt =
                userProblemStatusRepository.findByUserIdAndProblemId(userId, problemId);

        boolean favorite = statusOpt.map(UserProblemStatus::isFavorited).orElse(false);
        boolean solved = statusOpt.map(UserProblemStatus::isSolved).orElse(false);

        SolutionDTO solutionDTO = null;
        if (problem.getSolution() != null) {
            Map<String, Object> solution = problem.getSolution();
            String approach = (String) solution.getOrDefault("approach", "");
            String code = (String) solution.getOrDefault("code", "");
            String timeComplexity = (String) solution.getOrDefault("time_complexity", "");
            String spaceComplexity = (String) solution.getOrDefault("space_complexity", "");
            solutionDTO = new SolutionDTO(approach, code, timeComplexity, spaceComplexity);
        }

        return new ProblemDetailDTO(
                problem.getId(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getDescription(),
                favorite,
                solved,
                solutionDTO
        );
    }

    public ProblemListDTO updateProblemStatus(
            Long userId,
            Long problemId,
            boolean isSolved,
            boolean isFavorite
    ) {
        System.out.println("Updating problem status for problemId: " + problemId + ", userId: " + userId + ", isSolved: " + isSolved + ", isFavorite: " + isFavorite);
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found with ID: " + problemId));

        UserProblemStatus status = userProblemStatusRepository
                .findByUserIdAndProblemId(userId, problemId)
                .orElse(new UserProblemStatus(userId, problemId, false, false));

        status.setSolved(isSolved);
        status.setFavorited(isFavorite);
        userProblemStatusRepository.save(status);

        return new ProblemListDTO(
                problem.getId(),
                problem.getTitle(),
                problem.getDifficulty(),
                status.isFavorited(),
                status.isSolved()
        );
    }
}