package com.makersworld.oauth_inclass.controller;

import com.makersworld.oauth_inclass.dto.UserProfileDto;
import com.makersworld.oauth_inclass.service.UserProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.makersworld.oauth_inclass.dto.UpdateProfileRequest;

@Tag(name = "User Profile", description = "사용자 프로필 관리 엔드포인트")
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "사용자 프로필 조회", description = "인증된 사용자의 프로필을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDto profile = userProfileService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "사용자 프로필 업데이트", description = "인증된 사용자의 프로필을 업데이트합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        UserProfileDto updatedProfile = userProfileService.updateUserProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }
}
