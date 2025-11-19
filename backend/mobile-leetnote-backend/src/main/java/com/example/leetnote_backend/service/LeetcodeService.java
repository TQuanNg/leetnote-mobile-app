package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.GraphQLResponse;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.model.entity.UserLeetcodeProfile;
import com.example.leetnote_backend.repository.UserLeetcodeProfileRepository;
import com.example.leetnote_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeetcodeService {
    private final RestTemplate restTemplate;
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
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Fetch fresh stats from LeetCode API
        LeetcodeStatsDTO stats = fetchStatsFromLeetcodeAPI(leetcodeUsername);

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
                .orElseThrow(() -> new RuntimeException("No LeetCode profile found for user. Please set username first."));

        // Fetch fresh stats from LeetCode API
        LeetcodeStatsDTO stats = fetchStatsFromLeetcodeAPI(profile.getUsername());

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
     * Fetch stats from LeetCode GraphQL API
     * Cached by username for 10 minutes to avoid hitting LeetCode API too often
     */
    @Cacheable(value = "leetcodeApiStats", key = "#username")
    public LeetcodeStatsDTO fetchStatsFromLeetcodeAPI(String username) {
        String url = "https://leetcode.com/graphql";
        String query = """
                    query getUserProfile($username: String!) {
                      matchedUser(username: $username) {
                        submitStats {
                          acSubmissionNum {
                            difficulty
                            count
                            submissions
                          }
                        }
                      }
                    }
                """;

        Map<String, Object> request = Map.of(
                "query", query,
                "variables", Map.of("username", username)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("User-Agent", "Mozilla/5.0");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<GraphQLResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, GraphQLResponse.class);

        GraphQLResponse body = response.getBody();

        if (body == null || body.getData() == null || body.getData().getMatchedUser() == null) {
            throw new RuntimeException("User not found: " + username);
        }

        // Extract stats
        int easy = 0, medium = 0, hard = 0, total = 0;

        for (GraphQLResponse.DataNode.AcSubmissionNum stat :
                body.getData().getMatchedUser().getSubmitStats().getAcSubmissionNum()) {

            switch (stat.getDifficulty().toLowerCase()) {
                case "easy" -> easy = stat.getCount();
                case "medium" -> medium = stat.getCount();
                case "hard" -> hard = stat.getCount();
                case "all" -> total = stat.getCount();
            }
        }

        return new LeetcodeStatsDTO(username, total, easy, medium, hard);
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

