package com.example.leetnote_backend.repository;

import com.example.leetnote_backend.model.entity.UserLeetcodeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLeetcodeProfileRepository extends JpaRepository<UserLeetcodeProfile, Long> {
    @Override
    Optional<UserLeetcodeProfile> findById(Long aLong);
}
