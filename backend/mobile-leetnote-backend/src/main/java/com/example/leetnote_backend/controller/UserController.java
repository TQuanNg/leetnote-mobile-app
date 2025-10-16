package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.UpdateProfileRequest;
import com.example.leetnote_backend.model.DTO.UpdateUsernameRequest;
import com.example.leetnote_backend.model.DTO.UserProfileDTO;
import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();

        User user = userService.findById(userId); // Still fine
        UserProfileDTO dto = new UserProfileDTO(user.getId(), user.getEmail(), user.getUsername(), user.getProfileUrl());

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/username")
    public ResponseEntity<UserProfileDTO> setUsername(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateUsernameRequest request
    ) {
        Long userId = userPrincipal.getUserId();

        User updatedUser = userService.updateUsername(userId, request.getUsername());

        UserProfileDTO dto = new UserProfileDTO(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getUsername(),
                updatedUser.getProfileUrl()
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateProfileRequest request) {

        Long userId = userPrincipal.getUserId();
        User user = userService.updateProfileUrl(userId, request.getProfileUrl());
        return ResponseEntity.ok(user.getProfileUrl());
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<Void> deleteProfilePicture(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();
        userService.updateProfileUrl(userId, null);
        return ResponseEntity.noContent().build();
    }
}
