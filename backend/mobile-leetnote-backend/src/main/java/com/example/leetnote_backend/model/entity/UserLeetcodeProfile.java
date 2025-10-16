package com.example.leetnote_backend.model.entity;

import com.example.leetnote_backend.util.JsonToMapConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_leetcode_profiles")
public class UserLeetcodeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "leetcode_username", nullable = false, unique = true)
    private String username;

    @Column(name = "total_solved")
    public int totalSolved;

    @Column(name = "easy_solved")
    public int easySolved;

    @Column(name = "medium_solved")
    public int mediumSolved;

    @Column(name = "hard_solved")
    public int hardSolved;

    @Column(name = "submission_calendar", columnDefinition = "jsonb")
    @Convert(converter = JsonToMapConverter.class)
    public Map<String, Integer> submissionCalendar;

    @Column(name = "last_updated")
    public LocalDateTime lastUpdated;
}

