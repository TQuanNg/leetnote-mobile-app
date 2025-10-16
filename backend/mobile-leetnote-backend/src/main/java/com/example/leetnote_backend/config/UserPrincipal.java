package com.example.leetnote_backend.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.security.Principal;

@AllArgsConstructor
@Getter
@Setter
public class UserPrincipal implements Principal { // for authentication context only
    private Long userId;
    private final String uid;
    private final String email;

    @Override
    public String getName() {
        return uid;
    }
}
