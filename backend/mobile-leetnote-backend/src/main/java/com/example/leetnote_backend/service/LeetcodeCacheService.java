package com.example.leetnote_backend.service;

import com.example.leetnote_backend.exception.ResourceNotFoundException;
import com.example.leetnote_backend.model.DTO.GraphQLResponse;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service for fetching data from LeetCode GraphQL API
 * Separated to enable proper Spring cache proxying
 */
@Service
@RequiredArgsConstructor
public class LeetcodeCacheService {

    private final RestTemplate restTemplate;

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
            throw new ResourceNotFoundException("LeetCode user", "username", username);
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
}
