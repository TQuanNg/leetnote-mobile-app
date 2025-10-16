package com.example.leetnote_backend.model.entity;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ProblemSpecification {

    public static Specification<Problem> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String normalizedKeyword = keyword.trim().toLowerCase();

            // Split by spaces to handle multi-word queries
            String[] words = normalizedKeyword.split("\\s+");

            // Build a predicate that checks if the title contains all words in order
            Predicate finalPredicate = criteriaBuilder.conjunction();

            for (String word : words) {
                // Remove special chars for more robust matching
                String cleanWord = word.replaceAll("[^a-z0-9]", "");
                finalPredicate = criteriaBuilder.and(finalPredicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + cleanWord + "%"));
            }

            return finalPredicate;
        };
    }

    public static Specification<Problem> hasDifficulty(List<String> difficulties) {
        return (root, query, criteriaBuilder) -> {
            if (difficulties == null || difficulties.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("difficulty").in(difficulties);
        };
    }

    public static Specification<Problem> isSolved(Boolean solved, Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (solved == null) {
                return criteriaBuilder.conjunction();
            }

            var join = root.join("userProblemStatuses", JoinType.LEFT); // Left join to include problems without status
            var userFilter = criteriaBuilder.equal(join.get("userId"), userId);

            // If no status exists, consider solved=false
            var solvedExpr = criteriaBuilder.coalesce(join.get("isSolved"), false);
            return criteriaBuilder.and(userFilter, criteriaBuilder.equal(solvedExpr, solved));
        };
    }

    public static Specification<Problem> isFavorited(Boolean favorited, Long userId) {
        return (root, query, cb) -> {
            if (favorited == null) {
                return cb.conjunction();
            }
            var join = root.join("userProblemStatuses", JoinType.LEFT);
            var favExpr = cb.coalesce(
                    cb.selectCase()
                            .when(cb.equal(join.get("userId"), userId), join.get("isFavorited"))
                            .otherwise(false),
                    false
            );

            return cb.equal(favExpr, favorited);
        };
    }
}
