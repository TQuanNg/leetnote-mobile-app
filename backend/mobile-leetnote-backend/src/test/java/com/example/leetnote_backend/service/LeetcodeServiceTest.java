package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.DTO.LeetcodeStatsDTO;
import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.model.entity.UserLeetcodeProfile;
import com.example.leetnote_backend.repository.UserLeetcodeProfileRepository;
import com.example.leetnote_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeetcodeServiceTest {

    @Mock
    private LeetcodeCacheService leetcodeCacheService;

    @Mock
    private UserLeetcodeProfileRepository userLeetcodeProfileRepository;

    @Mock
    private UserRepository userRepository;

    private LeetcodeService service;

    @BeforeEach
    void setUp() {
        this.service = new LeetcodeService(leetcodeCacheService, userLeetcodeProfileRepository, userRepository);
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

        User user = new User();
        user.setId(userId);

        LeetcodeStatsDTO mockStats = new LeetcodeStatsDTO(username, 50, 20, 20, 10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(leetcodeCacheService.fetchStatsFromLeetcodeAPI(username)).thenReturn(mockStats);
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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

        verify(leetcodeCacheService).fetchStatsFromLeetcodeAPI(username);
    }

    @Test
    @DisplayName("saveLeetcodeUsername updates existing profile")
    void saveLeetcodeUsername_UpdatesExisting_WhenProfileExists() {
        Long userId = 1L;
        String username = "updateduser";

        User user = new User();
        user.setId(userId);

        UserLeetcodeProfile existingProfile = new UserLeetcodeProfile();
        existingProfile.setId(10L);
        existingProfile.setUser(user);
        existingProfile.setUsername("olduser");
        existingProfile.setTotalSolved(30);

        LeetcodeStatsDTO mockStats = new LeetcodeStatsDTO(username, 60, 25, 25, 10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));
        when(leetcodeCacheService.fetchStatsFromLeetcodeAPI(username)).thenReturn(mockStats);
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeetcodeStatsDTO result = service.saveLeetcodeUsername(userId, username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(60, result.getTotalSolved());

        verify(userLeetcodeProfileRepository).save(existingProfile);
        assertEquals(username, existingProfile.getUsername());
        assertEquals(60, existingProfile.getTotalSolved());

        verify(leetcodeCacheService).fetchStatsFromLeetcodeAPI(username);
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
        verify(leetcodeCacheService, never()).fetchStatsFromLeetcodeAPI(any());
    }

    @Test
    @DisplayName("saveLeetcodeUsername throws when LeetCode user not found")
    void saveLeetcodeUsername_Throws_WhenLeetcodeUserNotFound() {
        Long userId = 1L;
        String username = "nonexistent";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(leetcodeCacheService.fetchStatsFromLeetcodeAPI(username))
                .thenThrow(new RuntimeException("User not found: " + username));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.saveLeetcodeUsername(userId, username));
        assertTrue(ex.getMessage().contains("User not found: " + username));

        verify(userLeetcodeProfileRepository, never()).save(any());
        verify(leetcodeCacheService).fetchStatsFromLeetcodeAPI(username);
    }

    // ========== refreshStats tests ==========

    @Test
    @DisplayName("refreshStats fetches new data and updates database")
    void refreshStats_UpdatesDatabase_Success() {
        Long userId = 1L;
        String username = "testuser";

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

        LeetcodeStatsDTO mockStats = new LeetcodeStatsDTO(username, 105, 42, 36, 27);

        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(leetcodeCacheService.fetchStatsFromLeetcodeAPI(username)).thenReturn(mockStats);
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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

        verify(leetcodeCacheService).fetchStatsFromLeetcodeAPI(username);
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
        verify(leetcodeCacheService, never()).fetchStatsFromLeetcodeAPI(any());
    }

    // ========== updateLeetcodeUsername tests ==========

    @Test
    @DisplayName("updateLeetcodeUsername calls saveLeetcodeUsername")
    void updateLeetcodeUsername_CallsSave() {
        Long userId = 1L;
        String newUsername = "newusername";

        User user = new User();
        user.setId(userId);

        LeetcodeStatsDTO mockStats = new LeetcodeStatsDTO(newUsername, 75, 30, 30, 15);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userLeetcodeProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(leetcodeCacheService.fetchStatsFromLeetcodeAPI(newUsername)).thenReturn(mockStats);
        when(userLeetcodeProfileRepository.save(any(UserLeetcodeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeetcodeStatsDTO result = service.updateLeetcodeUsername(userId, newUsername);

        assertNotNull(result);
        assertEquals(newUsername, result.getUsername());
        assertEquals(75, result.getTotalSolved());

        verify(userRepository).findById(userId);
        verify(userLeetcodeProfileRepository).save(any(UserLeetcodeProfile.class));
        verify(leetcodeCacheService).fetchStatsFromLeetcodeAPI(newUsername);
    }
}
