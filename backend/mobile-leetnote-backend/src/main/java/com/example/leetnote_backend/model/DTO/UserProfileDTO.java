package com.example.leetnote_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfileDTO {
    private Long userId;
    private String email;
    private String username;
    private String profileUrl;
}
