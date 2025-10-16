package com.example.leetnote_backend.model.entity;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
//// This class represents a composite key for the UserProblemStatus entity.
public class UserProblemStatusId implements Serializable {
    public Long userId;
    public Long problemId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProblemStatusId that)) return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(problemId, that.problemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, problemId);
    }
}
