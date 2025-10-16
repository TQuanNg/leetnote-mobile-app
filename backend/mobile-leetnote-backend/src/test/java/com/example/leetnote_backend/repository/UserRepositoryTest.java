package com.example.leetnote_backend.repository;

import com.example.leetnote_backend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirebaseUid("firebase123");
        user = userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testFindByEmail() {
        User user = new User();
        user.setEmail("unique@example.com");
        user.setFirebaseUid("firebase456");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("unique@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirebaseUid()).isEqualTo("firebase456");
    }

    @Test
    public void testFindByFirebaseUid() {
        User user = new User();
        user.setEmail("another@example.com");
        user.setFirebaseUid("firebase789");
        userRepository.save(user);

        Optional<User> found = userRepository.findByFirebaseUid("firebase789");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("another@example.com");
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<User> found = userRepository.findById(999L);
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("notfound@example.com");
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindByFirebaseUidNotFound() {
        Optional<User> found = userRepository.findByFirebaseUid("nonexistent");
        assertThat(found).isNotPresent();
    }
}
