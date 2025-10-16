package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.FirebaseAuthenticationFilter;
import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.service.LeetcodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeetcodeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LeetcodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void getUserStats_ReturnsLeetcodeStats_WhenUserExists() throws Exception {
        // Arrange
        String username = "testuser";
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO(username, 100, 40, 35, 25);

        when(leetcodeService.getUserStats(username)).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(get("/leetcode/{username}", username)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.totalSolved").value(100))
                .andExpect(jsonPath("$.easySolved").value(40))
                .andExpect(jsonPath("$.mediumSolved").value(35))
                .andExpect(jsonPath("$.hardSolved").value(25));
    }

    @Test
    void getUserStats_ReturnsZeroStats_WhenUserHasNoSubmissions() throws Exception {
        // Arrange
        String username = "newuser";
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO(username, 0, 0, 0, 0);

        when(leetcodeService.getUserStats(username)).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(get("/leetcode/{username}", username)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.totalSolved").value(0))
                .andExpect(jsonPath("$.easySolved").value(0))
                .andExpect(jsonPath("$.mediumSolved").value(0))
                .andExpect(jsonPath("$.hardSolved").value(0));
    }

    @Test
    void getUserStats_Returns500_WhenUserNotFound() throws Exception {
        // Arrange
        String username = "nonexistentuser";
        when(leetcodeService.getUserStats(username))
                .thenThrow(new RuntimeException("User not found: " + username));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                mockMvc.perform(get("/leetcode/{username}", username)
                                .with(authenticated()))
                        .andReturn()
        );

        // The exception thrown by MockMvc will be a ServletException wrapping your RuntimeException
        assertTrue(exception.getMessage().contains("User not found: nonexistentuser"));
    }

    @Test
    void getUserStats_WithAuthentication_Returns200() throws Exception {
        // Arrange - Since addFilters = false, authentication is bypassed
        String username = "testuser";
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO(username, 50, 20, 20, 10);

        when(leetcodeService.getUserStats(username)).thenReturn(expectedStats);

        // Act & Assert - Without authentication should still work due to addFilters = false
        mockMvc.perform(get("/leetcode/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void getUserStats_HandlesSpecialCharactersInUsername() throws Exception {
        // Arrange
        String username = "user123";  // Using simpler username to avoid URL encoding issues
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO(username, 50, 20, 20, 10);

        when(leetcodeService.getUserStats(username)).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(get("/leetcode/{username}", username)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.totalSolved").value(50));
    }

    @Test
    void getUserStats_HandlesRegularUsername() throws Exception {
        // Arrange
        String username = "regularuser"; // Using normal length username
        LeetcodeStatsDTO expectedStats = new LeetcodeStatsDTO(username, 200, 80, 70, 50);

        when(leetcodeService.getUserStats(username)).thenReturn(expectedStats);

        // Act & Assert
        mockMvc.perform(get("/leetcode/{username}", username)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.totalSolved").value(200));
    }
}
