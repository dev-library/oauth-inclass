package com.makersworld.oauth_inclass.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String bio;
    private String location;
    private String website;
    private String phoneNumber;
    private String avatarUrl;
} 