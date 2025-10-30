package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.service.LeetcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leetcode")
@RequiredArgsConstructor
public class LeetcodeController {

    private final LeetcodeService leetcodeService;

    /**
     * Get user's stored LeetCode stats from database
     * Returns null if user hasn't set their LeetCode username yet
     */
    @GetMapping("/profile")
    public ResponseEntity<LeetcodeStatsDTO> getUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();
        LeetcodeStatsDTO stats = leetcodeService.getUserStats(userId);

        if (stats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * Set or update LeetCode username and fetch fresh stats
     * This will save the username and stats to database
     */
    @PostMapping("/username")
    public ResponseEntity<LeetcodeStatsDTO> setLeetcodeUsername(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SetUsernameRequest request
    ) {
        Long userId = userPrincipal.getUserId();
        LeetcodeStatsDTO stats = leetcodeService.saveLeetcodeUsername(userId, request.getUsername());
        return ResponseEntity.ok(stats);
    }

    /**
     * Update/change LeetCode username to a new one
     */
    @PutMapping("/username")
    public ResponseEntity<LeetcodeStatsDTO> updateLeetcodeUsername(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SetUsernameRequest request
    ) {
        Long userId = userPrincipal.getUserId();
        LeetcodeStatsDTO stats = leetcodeService.updateLeetcodeUsername(userId, request.getUsername());
        return ResponseEntity.ok(stats);
    }

    /**
     * Refresh stats from LeetCode API (for existing username)
     * Use this when user wants to update their stats manually
     */
    @PostMapping("/refresh")
    public ResponseEntity<LeetcodeStatsDTO> refreshStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();
        LeetcodeStatsDTO stats = leetcodeService.refreshStats(userId);
        return ResponseEntity.ok(stats);
    }

    // Request DTO
    @lombok.Data
    public static class SetUsernameRequest {
        private String username;
    }
}
