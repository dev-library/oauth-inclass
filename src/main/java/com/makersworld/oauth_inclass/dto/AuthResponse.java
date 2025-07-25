package com.makersworld.oauth_inclass.dto;

import com.makersworld.oauth_inclass.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String email;
    private String name;
    private Role role;
} 