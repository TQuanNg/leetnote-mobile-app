package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
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

    @Transactional
    public User updateUsername(Long userId, String username) {
        User user = findById(userId);
        user.setUsername(username);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfileUrl(Long userId, String profileUrl) {
        User user = findById(userId);

        user.setProfileUrl(profileUrl);
        return userRepository.save(user);
    }
}
