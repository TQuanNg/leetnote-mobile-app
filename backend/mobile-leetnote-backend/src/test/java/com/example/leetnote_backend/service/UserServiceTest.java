package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("oldUsername");
        user.setFirebaseUid("firebase-123");
    }

    @Test
    void testFindById_UserExits() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id");
    }

    @Test
    void testFindOrCreateUser_UserExists_ReturnsExisting() {
        when(userRepository.findByFirebaseUid("firebase-123")).thenReturn(Optional.of(user));

        User result = userService.findOrCreateUser("firebase-123", "test@example.com");

        assertThat(result).isEqualTo(user);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testFindOrCreateUser_UserNotExists_CreatesNewUser() {
        when(userRepository.findByFirebaseUid("firebase-999")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        User result = userService.findOrCreateUser("firebase-999", "new@example.com");

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUsername_UpdatesAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUsername(1L, "newUsername");

        assertThat(result.getUsername()).isEqualTo("newUsername");
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateProfileUrl_UpdatesAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateProfileUrl(1L, "http://image.com/pic.png");

        assertThat(result.getProfileUrl()).isEqualTo("http://image.com/pic.png");
        verify(userRepository).save(user);
    }

}
