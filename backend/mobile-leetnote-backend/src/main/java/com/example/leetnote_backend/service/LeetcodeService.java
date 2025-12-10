package com.example.leetnote_backend.service;

import com.example.leetnote_backend.exception.BadRequestException;
import com.example.leetnote_backend.exception.ResourceNotFoundException;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.model.entity.UserLeetcodeProfile;
import com.example.leetnote_backend.repository.UserLeetcodeProfileRepository;
import com.example.leetnote_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeetcodeService {

    private final LeetcodeCacheService leetcodeApiService;
    private final UserLeetcodeProfileRepository userLeetcodeProfileRepository;
    private final UserRepository userRepository;

    /**
     * Get user's LeetCode stats from database (if exists), otherwise return null
     * Cached for 5 minutes to reduce database hits
     */
    @Cacheable(value = "userLeetcodeStats", key = "#userId")
    public LeetcodeStatsDTO getUserStats(Long userId) {
        return userLeetcodeProfileRepository.findByUserId(userId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Save or update LeetCode username for a user and fetch fresh stats from LeetCode API
     * Evicts cache after updating
     */
    @CacheEvict(value = "userLeetcodeStats", key = "#userId")
    public LeetcodeStatsDTO saveLeetcodeUsername(Long userId, String leetcodeUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Fetch fresh stats from LeetCode API (now uses separate service - caching works!)
        LeetcodeStatsDTO stats = leetcodeApiService.fetchStatsFromLeetcodeAPI(leetcodeUsername);

        // Find existing profile or create new one
        UserLeetcodeProfile profile = userLeetcodeProfileRepository.findByUserId(userId)
                .orElse(new UserLeetcodeProfile());

        // Update profile
        profile.setUser(user);
        profile.setUsername(leetcodeUsername);
        profile.setTotalSolved(stats.getTotalSolved());
        profile.setEasySolved(stats.getEasySolved());
        profile.setMediumSolved(stats.getMediumSolved());
        profile.setHardSolved(stats.getHardSolved());
        profile.setLastUpdated(LocalDateTime.now());

        userLeetcodeProfileRepository.save(profile);

        return stats;
    }

    /**
     * Refresh stats from LeetCode API for existing user
     * Evicts cache after updating
     */
    @CacheEvict(value = "userLeetcodeStats", key = "#userId")
    public LeetcodeStatsDTO refreshStats(Long userId) {
        UserLeetcodeProfile profile = userLeetcodeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No LeetCode profile found for user. Please set username first."));

        // Fetch fresh stats from LeetCode API (now uses separate service - caching works!)
        LeetcodeStatsDTO stats = leetcodeApiService.fetchStatsFromLeetcodeAPI(profile.getUsername());

        // Update profile
        profile.setTotalSolved(stats.getTotalSolved());
        profile.setEasySolved(stats.getEasySolved());
        profile.setMediumSolved(stats.getMediumSolved());
        profile.setHardSolved(stats.getHardSolved());
        profile.setLastUpdated(LocalDateTime.now());

        userLeetcodeProfileRepository.save(profile);

        return stats;
    }

    /**
     * Update LeetCode username (change it)
     */
    @CacheEvict(value = "userLeetcodeStats", key = "#userId")
    public LeetcodeStatsDTO updateLeetcodeUsername(Long userId, String newUsername) {
        return saveLeetcodeUsername(userId, newUsername);
    }

    /**
     * Convert UserLeetcodeProfile entity to DTO
     */
    private LeetcodeStatsDTO convertToDTO(UserLeetcodeProfile profile) {
        return new LeetcodeStatsDTO(
                profile.getUsername(),
                profile.getTotalSolved(),
                profile.getEasySolved(),
                profile.getMediumSolved(),
                profile.getHardSolved()
        );
    }
}
