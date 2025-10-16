package com.example.leetnote_backend.controller;

import com.example.leetnote_backend.config.FirebaseAuthenticationFilter;
import com.example.leetnote_backend.config.UserPrincipal;
import com.example.leetnote_backend.model.DTO.UpdateProfileRequest;
import com.example.leetnote_backend.model.DTO.UpdateUsernameRequest;
import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.service.UserService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FirebaseAuthenticationFilter firebaseAuthenticationFilter; // disable the Firebase filter

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
    void getUserProfile_ReturnsUserProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("username");
        user.setProfileUrl("profile.jpg");

        when(userService.findById(anyLong())).thenReturn(user);

        UserPrincipal principal = new UserPrincipal(1L, "firebase-123", "test@example.com");

        // Create an Authentication and SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile").with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.profileUrl").value("profile.jpg"));
    }

    @Test
    void setUsername_UpdatesUsername_ReturnsUpdatedUserProfile() throws Exception {
        UpdateUsernameRequest request = new UpdateUsernameRequest();
        request.setUsername("newUsername");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("test@example.com");
        updatedUser.setUsername("newUsername");
        updatedUser.setProfileUrl("profile.jpg");

        when(userService.updateUsername(anyLong(), eq("newUsername"))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/username")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUsername\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("newUsername"));
    }

    @Test
    void uploadProfilePicture_UpdatesProfilePicture_ReturnsUrl() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setProfileUrl("newProfile.jpg");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setProfileUrl("newProfile.jpg");

        when(userService.updateProfileUrl(anyLong(), eq("newProfile.jpg"))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/profile-picture")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profileUrl\":\"newProfile.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("newProfile.jpg"));
    }

    @Test
    void deleteProfilePicture_DeletesProfilePicture_ReturnsNoContent() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setProfileUrl(null);

        // Mock the service to return a User even when setting profileUrl to null
        when(userService.updateProfileUrl(anyLong(), eq(null))).thenReturn(updatedUser);

        mockMvc.perform(delete("/api/users/profile-picture")
                        .with(authenticated()))
                .andExpect(status().isNoContent());
    }
}