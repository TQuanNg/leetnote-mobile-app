package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserCacheService userCacheService;

    /**
     * Find user by ID - cached for quick access
     */
    public User findById(Long id) {
        return userCacheService.findById(id);
    }

    public User findOrCreateUser(String firebaseUid, String email) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setFirebaseUid(firebaseUid);
                    newUser.setEmail(email);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }

    /**
     * Update username - evicts user cache
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public User updateUsername(Long userId, String username) {
        User user = findById(userId);
        user.setUsername(username);
        return userRepository.save(user);
    }

    /**
     * Update profile URL - evicts user cache
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public User updateProfileUrl(Long userId, String profileUrl) {
        User user = findById(userId);
        user.setProfileUrl(profileUrl);
        return userRepository.save(user);
    }
}
