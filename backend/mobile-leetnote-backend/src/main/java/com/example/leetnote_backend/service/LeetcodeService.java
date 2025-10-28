package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.GraphQLResponse;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeetcodeService {

    private final RestTemplate restTemplate;

    @Cacheable(value = "leetcodeProfiles", key = "#username" )
    public LeetcodeStatsDTO getUserStats(String username) {
        String url = "https://leetcode.com/graphql" + username;
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
}

