package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.service.LeetcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leetcode")
@RequiredArgsConstructor
public class LeetcodeController {

    private final LeetcodeService leetCodeService;

    @GetMapping("/{username}")
    public ResponseEntity<LeetcodeStatsDTO> getUserStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String username
    ) {
        LeetcodeStatsDTO stats = leetCodeService.getUserStats(username);
        return ResponseEntity.ok(stats);
    }
}
