package com.example.leetnote_backend.repository;

import com.example.leetnote_backend.model.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findTopBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);
    List<Evaluation> findBySubmission_UserIdAndSubmission_ProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);
    
    @Query("SELECT e FROM Evaluation e WHERE e.id IN (" +
           "SELECT MAX(e2.id) FROM Evaluation e2 " +
           "WHERE e2.submission.userId = :userId " +
           "GROUP BY e2.submission.problemId) " +
           "ORDER BY e.createdAt DESC")
    List<Evaluation> findLatestEvaluationsByUserId(@Param("userId") Long userId);
}