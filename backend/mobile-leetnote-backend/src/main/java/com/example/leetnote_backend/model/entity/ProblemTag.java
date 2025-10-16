package com.example.leetnote_backend.model.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "problem_tags")
public class ProblemTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
}
