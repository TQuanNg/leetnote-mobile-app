package com.example.leetnote_backend.repository;

import com.example.leetnote_backend.model.entity.UserProblemStatus;
import com.example.leetnote_backend.model.entity.UserProblemStatusId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProblemStatusRepository extends JpaRepository<UserProblemStatus, UserProblemStatusId> {
    Optional<UserProblemStatus> findById(UserProblemStatusId id);
    List<UserProblemStatus> findAllByUserId(Long userId);
    Optional<UserProblemStatus> findByUserIdAndProblemId(Long userId, Long problemId);
}
