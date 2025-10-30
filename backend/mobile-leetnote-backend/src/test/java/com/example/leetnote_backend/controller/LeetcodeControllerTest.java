package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.FirebaseAuthenticationFilter;
import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.service.LeetcodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeetcodeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LeetcodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeetcodeService leetcodeService;

    @MockitoBean
    private FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    private RequestPostProcessor authenticated() {
        UserPrincipal principal = new UserPrincipal(1L, "firebase-123", "test@example.com");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        return SecurityMockMvcRequestPostProcessors.securityContext(context);
    }

    @Test
    void getUserProfile_ReturnsLeetcodeStats_WhenProfileExists() throws Exception {
        // Arrange
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO("testuser", 100, 40, 35, 25);

        when(leetcodeService.getUserStats(1L)).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(get("/api/leetcode/profile")
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.totalSolved").value(100))
                .andExpect(jsonPath("$.easySolved").value(40))
                .andExpect(jsonPath("$.mediumSolved").value(35))
                .andExpect(jsonPath("$.hardSolved").value(25));
    }

    @Test
    void getUserProfile_Returns404_WhenProfileNotFound() throws Exception {
        // Arrange
        when(leetcodeService.getUserStats(1L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/leetcode/profile")
                        .with(authenticated()))
                .andExpect(status().isNotFound());
    }

    @Test
    void setLeetcodeUsername_ReturnsStats_WhenSuccessful() throws Exception {
        // Arrange
        String username = "newuser";
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO(username, 50, 20, 20, 10);
        LeetcodeController.SetUsernameRequest request = new LeetcodeController.SetUsernameRequest();
        request.setUsername(username);

        when(leetcodeService.saveLeetcodeUsername(eq(1L), eq(username))).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(post("/api/leetcode/username")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.totalSolved").value(50))
                .andExpect(jsonPath("$.easySolved").value(20))
                .andExpect(jsonPath("$.mediumSolved").value(20))
                .andExpect(jsonPath("$.hardSolved").value(10));
    }

    @Test
    void updateLeetcodeUsername_ReturnsNewStats_WhenSuccessful() throws Exception {
        // Arrange
        String newUsername = "updateduser";
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO(newUsername, 150, 60, 55, 35);
        LeetcodeController.SetUsernameRequest request = new LeetcodeController.SetUsernameRequest();
        request.setUsername(newUsername);

        when(leetcodeService.updateLeetcodeUsername(eq(1L), eq(newUsername))).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(put("/api/leetcode/username")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(newUsername))
                .andExpect(jsonPath("$.totalSolved").value(150))
                .andExpect(jsonPath("$.easySolved").value(60))
                .andExpect(jsonPath("$.mediumSolved").value(55))
                .andExpect(jsonPath("$.hardSolved").value(35));
    }

    @Test
    void refreshStats_ReturnsUpdatedStats_WhenSuccessful() throws Exception {
        // Arrange
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO("testuser", 105, 42, 36, 27);

        when(leetcodeService.refreshStats(1L)).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(post("/api/leetcode/refresh")
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.totalSolved").value(105))
                .andExpect(jsonPath("$.easySolved").value(42))
                .andExpect(jsonPath("$.mediumSolved").value(36))
                .andExpect(jsonPath("$.hardSolved").value(27));
    }

    @Test
    void setLeetcodeUsername_Returns500_WhenLeetcodeUserNotFound() throws Exception {
        // Arrange
        String username = "nonexistentuser";
        LeetcodeController.SetUsernameRequest request = new LeetcodeController.SetUsernameRequest();
        request.setUsername(username);

        when(leetcodeService.saveLeetcodeUsername(eq(1L), eq(username)))
                .thenThrow(new RuntimeException("User not found: " + username));

        // Act & Assert
        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/leetcode/username")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
        );
    }

    @Test
    void refreshStats_Returns500_WhenNoProfileExists() throws Exception {
        // Arrange
        when(leetcodeService.refreshStats(1L))
                .thenThrow(new RuntimeException("No LeetCode profile found for user. Please set username first."));

        assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/leetcode/refresh")
                        .with(authenticated()))
        );
    }

    @Test
    void getUserProfile_WithZeroStats_ReturnsCorrectly() throws Exception {
        // Arrange
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO("newuser", 0, 0, 0, 0);

        when(leetcodeService.getUserStats(1L)).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(get("/api/leetcode/profile")
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.totalSolved").value(0))
                .andExpect(jsonPath("$.easySolved").value(0))
                .andExpect(jsonPath("$.mediumSolved").value(0))
                .andExpect(jsonPath("$.hardSolved").value(0));
    }
}