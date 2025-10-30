package com.example.leetnote_backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.*;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> solution;

    @OneToMany(mappedBy = "problems", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Example> examples;

    @OneToMany(mappedBy = "problems", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Constraint> constraints;

    @OneToMany(mappedBy = "problems", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProblemStatus> userProblemStatuses;
}