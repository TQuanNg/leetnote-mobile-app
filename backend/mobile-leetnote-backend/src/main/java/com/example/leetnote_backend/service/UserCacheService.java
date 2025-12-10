package com.example.leetnote_backend.service;

import com.example.leetnote_backend.model.entity.User;
import com.example.leetnote_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}
