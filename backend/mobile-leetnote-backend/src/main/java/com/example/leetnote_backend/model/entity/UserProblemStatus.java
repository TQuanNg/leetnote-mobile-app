package com.example.leetnote_backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_problem_status")
@IdClass(UserProblemStatusId.class)
public class UserProblemStatus {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "problem_id")
    private Long problemId;

    @Column(name = "is_solved", nullable = false)
    private boolean isSolved;

    @Column(name = "is_favorited", nullable = false)
    private boolean isFavorited;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", insertable = false, updatable = false)
    @JsonIgnore
    private Problem problems;

    public UserProblemStatus(Long userId, Long problemId, boolean isSolved, boolean isFavorited) {
        this.userId = userId;
        this.problemId = problemId;
        this.isSolved = isSolved;
        this.isFavorited = isFavorited;
    }
}
