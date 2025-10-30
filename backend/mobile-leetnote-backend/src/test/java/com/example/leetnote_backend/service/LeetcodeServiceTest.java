package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.model.entity.UserLeetcodeProfile;
import com.example.leetnote_backend.repository.UserLeetcodeProfileRepository;
import com.example.leetnote_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
public class LeetcodeServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    @Mock
    private UserLeetcodeProfileRepository userLeetcodeProfileRepository;

    @Mock
    private UserRepository userRepository;

    private LeetcodeService service;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        this.restTemplate = new RestTemplate();
        this.server = MockRestServiceServer.bindTo(restTemplate).build();
        this.service = new LeetcodeService(restTemplate, userLeetcodeProfileRepository, userRepository);
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

    // ========== getUserStats (from database) tests ==========

    @Test
    @DisplayName("getUserStats returns stats from database when profile exists")
    void getUserStats_ReturnsFromDatabase_WhenProfileExists() {
        Long userId = 1L;
        UserLeetcodeProfile profile = new UserLeetcodeProfile();
        profile.setUsername("testuser");
        profile.setTotalSolved(100);
        profile.setEasySolved(40);
        profile.setMediumSolved(35);
        profile.setHardSolved(25);

        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        LeetcodeStatsDTO result = service.getUserStats(userId);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(100, result.getTotalSolved());
        assertEquals(40, result.getEasySolved());
        assertEquals(35, result.getMediumSolved());
        assertEquals(25, result.getHardSolved());

        verify(userLeetcodeProfileRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("getUserStats returns null when profile does not exist")
    void getUserStats_ReturnsNull_WhenProfileNotExists() {
        Long userId = 1L;
        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        LeetcodeStatsDTO result = service.getUserStats(userId);

        assertNull(result);
        verify(userLeetcodeProfileRepository).findByUserId(userId);
    }

    // ========== saveLeetcodeUsername tests ==========

    @Test
    @DisplayName("saveLeetcodeUsername fetches from API and saves to database")
    void saveLeetcodeUsername_FetchesAndSaves_Success() {
        Long userId = 1L;
        String username = "newuser";
        String url = "https://leetcode.com/graphql";

        User user = new User();
        user.setId(userId);

        List<Map<String, Object>> arr = List.of(
                Map.of("difficulty", "All", "count", 50, "submissions", 100),
                Map.of("difficulty", "Easy", "count", 20, "submissions", 40),
                Map.of("difficulty", "Medium", "count", 20, "submissions", 40),
                Map.of("difficulty", "Hard", "count", 10, "submissions", 20)
        );
        String body = buildResponseJson(arr);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO result = service.saveLeetcodeUsername(userId, username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(50, result.getTotalSolved());
        assertEquals(20, result.getEasySolved());
        assertEquals(20, result.getMediumSolved());
        assertEquals(10, result.getHardSolved());

        ArgumentCaptor<UserLeetcodeProfile> captor = ArgumentCaptor.forClass(UserLeetcodeProfile.class);
        verify(userLeetcodeProfileRepository).save(captor.capture());

        UserLeetcodeProfile saved = captor.getValue();
        assertEquals(username, saved.getUsername());
        assertEquals(50, saved.getTotalSolved());
        assertEquals(20, saved.getEasySolved());
        assertEquals(20, saved.getMediumSolved());
        assertEquals(10, saved.getHardSolved());
        assertNotNull(saved.getLastUpdated());

        server.verify();
    }

    @Test
    @DisplayName("saveLeetcodeUsername updates existing profile")
    void saveLeetcodeUsername_UpdatesExisting_WhenProfileExists() {
        Long userId = 1L;
        String username = "updateduser";
        String url = "https://leetcode.com/graphql";

        User user = new User();
        user.setId(userId);

        UserLeetcodeProfile existingProfile = new UserLeetcodeProfile();
        existingProfile.setId(10L);
        existingProfile.setUser(user);
        existingProfile.setUsername("olduser");
        existingProfile.setTotalSolved(30);

        List<Map<String, Object>> arr = List.of(
                Map.of("difficulty", "All", "count", 60, "submissions", 120),
                Map.of("difficulty", "Easy", "count", 25, "submissions", 50),
                Map.of("difficulty", "Medium", "count", 25, "submissions", 50),
                Map.of("difficulty", "Hard", "count", 10, "submissions", 20)
        );
        String body = buildResponseJson(arr);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO result = service.saveLeetcodeUsername(userId, username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(60, result.getTotalSolved());

        verify(userLeetcodeProfileRepository).save(existingProfile);
        assertEquals(username, existingProfile.getUsername());
        assertEquals(60, existingProfile.getTotalSolved());

        server.verify();
    }

    @Test
    @DisplayName("saveLeetcodeUsername throws when user not found")
    void saveLeetcodeUsername_Throws_WhenUserNotFound() {
        Long userId = 999L;
        String username = "testuser";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.saveLeetcodeUsername(userId, username));
        assertTrue(ex.getMessage().contains("User not found with id: " + userId));

        verify(userLeetcodeProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveLeetcodeUsername throws when LeetCode user not found")
    void saveLeetcodeUsername_Throws_WhenLeetcodeUserNotFound() {
        Long userId = 1L;
        String username = "nonexistent";
        String url = "https://leetcode.com/graphql";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String body = "{\"data\": null}";

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.saveLeetcodeUsername(userId, username));
        assertTrue(ex.getMessage().contains("User not found: " + username));

        verify(userLeetcodeProfileRepository, never()).save(any());
        server.verify();
    }

    // ========== refreshStats tests ==========

    @Test
    @DisplayName("refreshStats fetches new data and updates database")
    void refreshStats_UpdatesDatabase_Success() {
        Long userId = 1L;
        String username = "testuser";
        String url = "https://leetcode.com/graphql";

        User user = new User();
        user.setId(userId);

        UserLeetcodeProfile profile = new UserLeetcodeProfile();
        profile.setId(5L);
        profile.setUser(user);
        profile.setUsername(username);
        profile.setTotalSolved(100);
        profile.setEasySolved(40);
        profile.setMediumSolved(35);
        profile.setHardSolved(25);
        profile.setLastUpdated(LocalDateTime.now().minusDays(1));

        List<Map<String, Object>> arr = List.of(
                Map.of("difficulty", "All", "count", 105, "submissions", 200),
                Map.of("difficulty", "Easy", "count", 42, "submissions", 80),
                Map.of("difficulty", "Medium", "count", 36, "submissions", 70),
                Map.of("difficulty", "Hard", "count", 27, "submissions", 50)
        );
        String body = buildResponseJson(arr);

        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO result = service.refreshStats(userId);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(105, result.getTotalSolved());
        assertEquals(42, result.getEasySolved());
        assertEquals(36, result.getMediumSolved());
        assertEquals(27, result.getHardSolved());

        verify(userLeetcodeProfileRepository).save(profile);
        assertEquals(105, profile.getTotalSolved());
        assertEquals(42, profile.getEasySolved());

        server.verify();
    }

    @Test
    @DisplayName("refreshStats throws when profile not found")
    void refreshStats_Throws_WhenProfileNotFound() {
        Long userId = 1L;

        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.refreshStats(userId));
        assertTrue(ex.getMessage().contains("No LeetCode profile found for user"));

        verify(userLeetcodeProfileRepository, never()).save(any());
    }

    // ========== updateLeetcodeUsername tests ==========

    @Test
    @DisplayName("updateLeetcodeUsername calls saveLeetcodeUsername")
    void updateLeetcodeUsername_CallsSave() {
        Long userId = 1L;
        String newUsername = "newusername";
        String url = "https://leetcode.com/graphql";

        User user = new User();
        user.setId(userId);

        List<Map<String, Object>> arr = List.of(
                Map.of("difficulty", "All", "count", 75, "submissions", 150),
                Map.of("difficulty", "Easy", "count", 30, "submissions", 60),
                Map.of("difficulty", "Medium", "count", 30, "submissions", 60),
                Map.of("difficulty", "Hard", "count", 15, "submissions", 30)
        );
        String body = buildResponseJson(arr);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LeetcodeStatsDTO result = service.updateLeetcodeUsername(userId, newUsername);

        assertNotNull(result);
        assertEquals(newUsername, result.getUsername());
        assertEquals(75, result.getTotalSolved());

        verify(userRepository).findById(userId);
        verify(userLeetcodeProfileRepository).save(any(UserLeetcodeProfile.class));

        server.verify();
    }
}
