package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class LeetcodeServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private LeetcodeService service;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        this.restTemplate = new RestTemplate();
        this.server = MockRestServiceServer.bindTo(restTemplate).build();
        this.service = new LeetcodeService(restTemplate);
        this.mapper = new ObjectMapper();
    }

    private String buildResponseJson(List<Map<String, Object>> acSubmissionNumList) {
        try {
            Map<String, Object> submitStats = new HashMap<>();
            submitStats.put("acSubmissionNum", acSubmissionNumList);

            Map<String, Object> matchedUser = new HashMap<>();
            matchedUser.put("submitStats", submitStats);

            Map<String, Object> data = new HashMap<>();
            data.put("matchedUser", matchedUser);

            Map<String, Object> root = new HashMap<>();
            root.put("data", data);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getUserStats returns parsed counts when GraphQL returns all difficulties including All")
    void getUserStats_Success_AllDifficulties() {
        String username = "testuser";
        String url = "https://leetcode.com/graphql" + username; // matches current service implementation

        List<Map<String, Object>> arr = List.of(
                Map.of("difficulty", "All", "count", 123, "submissions", 200),
                Map.of("difficulty", "Easy", "count", 50, "submissions", 80),
                Map.of("difficulty", "Medium", "count", 40, "submissions", 70),
                Map.of("difficulty", "Hard", "count", 33, "submissions", 50)
        );
        String body = buildResponseJson(arr);

        server.expect(requestTo(url))
                .andExpect(method(POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO dto = service.getUserStats(username);

        assertNotNull(dto);
        assertEquals(username, dto.getUsername());
        assertEquals(123, dto.getTotalSolved());
        assertEquals(50, dto.getEasySolved());
        assertEquals(40, dto.getMediumSolved());
        assertEquals(33, dto.getHardSolved());

        server.verify();
    }

    @Test
    @DisplayName("getUserStats sets total=0 when 'All' entry is missing")
    void getUserStats_MissingAll_TotalZero() {
        String username = "noall";
        String url = "https://leetcode.com/graphql" + username;

        List<Map<String, Object>> arr = List.of(
                Map.of("difficulty", "Easy", "count", 5, "submissions", 6),
                Map.of("difficulty", "Medium", "count", 7, "submissions", 8),
                Map.of("difficulty", "Hard", "count", 9, "submissions", 10)
        );
        String body = buildResponseJson(arr);

        server.expect(requestTo(url)).andExpect(method(POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO dto = service.getUserStats(username);

        assertEquals(0, dto.getTotalSolved());
        assertEquals(5, dto.getEasySolved());
        assertEquals(7, dto.getMediumSolved());
        assertEquals(9, dto.getHardSolved());

        server.verify();
    }

    @Test
    @DisplayName("getUserStats is case-insensitive for difficulty labels")
    void getUserStats_CaseInsensitive() {
        String username = "cases";
        String url = "https://leetcode.com/graphql" + username;

        List<Map<String, Object>> arr = List.of(
                Map.of("difficulty", "ALL", "count", 10, "submissions", 20),
                Map.of("difficulty", "EASY", "count", 1, "submissions", 2),
                Map.of("difficulty", "Medium", "count", 3, "submissions", 4),
                Map.of("difficulty", "hard", "count", 6, "submissions", 7)
        );
        String body = buildResponseJson(arr);

        server.expect(requestTo(url)).andExpect(method(POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO dto = service.getUserStats(username);

        assertEquals(10, dto.getTotalSolved());
        assertEquals(1, dto.getEasySolved());
        assertEquals(3, dto.getMediumSolved());
        assertEquals(6, dto.getHardSolved());

        server.verify();
    }

    @Test
    @DisplayName("getUserStats throws when body.data is null (user not found)")
    void getUserStats_UserNotFound_NullData() {
        String username = "nouser";
        String url = "https://leetcode.com/graphql" + username;

        String body = "{\"data\": null}";

        server.expect(requestTo(url)).andExpect(method(POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getUserStats(username));
        assertTrue(ex.getMessage().contains("User not found: " + username));

        server.verify();
    }

    @Test
    @DisplayName("getUserStats throws when matchedUser is null (user not found)")
    void getUserStats_UserNotFound_NullMatchedUser() {
        String username = "nouser2";
        String url = "https://leetcode.com/graphql" + username;

        String body = "{\n  \"data\": { \"matchedUser\": null }\n}";

        server.expect(requestTo(url)).andExpect(method(POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getUserStats(username));
        assertTrue(ex.getMessage().contains("User not found: " + username));

        server.verify();
    }

    @Test
    @DisplayName("getUserStats throws HttpClientErrorException on 404/4xx from LeetCode")
    void getUserStats_Http4xx_Propagates() {
        String username = "bad";
        String url = "https://leetcode.com/graphql" + username;

        server.expect(requestTo(url)).andExpect(method(POST))
                .andRespond(withBadRequest());

        assertThrows(HttpClientErrorException.class, () -> service.getUserStats(username));

        server.verify();
    }

    @Test
    @DisplayName("getUserStats returns zeros when acSubmissionNum is empty")
    void getUserStats_EmptyArray_ReturnsZeros() {
        String username = "empty";
        String url = "https://leetcode.com/graphql" + username;

        String body = buildResponseJson(List.of());

        server.expect(requestTo(url)).andExpect(method(POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO dto = service.getUserStats(username);

        assertEquals(username, dto.getUsername());
        assertEquals(0, dto.getTotalSolved());
        assertEquals(0, dto.getEasySolved());
        assertEquals(0, dto.getMediumSolved());
        assertEquals(0, dto.getHardSolved());

        server.verify();
    }
}
