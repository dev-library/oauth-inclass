package com.makersworld.oauth_inclass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("connected_at")
    private String connectedAt;
    
    @JsonProperty("properties")
    private Properties properties;
    
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        @JsonProperty("nickname")
        private String nickname;
        
        @JsonProperty("profile_image")
        private String profileImage;
        
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        @JsonProperty("profile_nickname_needs_agreement")
        private Boolean profileNicknameNeedsAgreement;
        
        @JsonProperty("profile_image_needs_agreement")
        private Boolean profileImageNeedsAgreement;
        
        @JsonProperty("profile")
        private Profile profile;
        
        @JsonProperty("has_email")
        private Boolean hasEmail;
        
        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;
        
        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;
        
        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;
        
        @JsonProperty("email")
        private String email;
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            @JsonProperty("nickname")
            private String nickname;
            
            @JsonProperty("thumbnail_image_url")
            private String thumbnailImageUrl;
            
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
            
            @JsonProperty("is_default_image")
            private Boolean isDefaultImage;
        }
    }
    
    // 편의 메서드들
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.getEmail() : null;
    }
    
    /**
     * 이메일 검증 및 기본값 제공 메서드
     * 카카오에서 이메일이 제공되지 않는 경우 더미 이메일 반환
     * 
     * TODO: 카카오 비즈니스 등록 후 실제 이메일 수집이 가능해지면 이 메서드 제거 필요
     * 
     * @return 유효한 이메일 또는 더미 이메일
     */
    public String getValidatedEmail() {
        String email = getEmail();
        if (email == null || email.trim().isEmpty()) {
            // 카카오에서 이메일을 제공하지 않는 경우 더미 이메일 생성
            // 사용자 ID를 포함하여 고유성 보장
            return "kakao_user_" + getId() + "@dummy.kakao.local";
        }
        return email;
    }
    
    /**
     * 닉네임 검증 및 기본값 제공 메서드
     * 카카오에서 닉네임이 제공되지 않는 경우 기본 닉네임 반환
     * 
     * TODO: 향후 닉네임 수집이 안정화되면 이 메서드 검토 필요
     * 
     * @return 유효한 닉네임 또는 기본 닉네임
     */
    public String getValidatedNickname() {
        String nickname = getNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            // 카카오에서 닉네임을 제공하지 않는 경우 기본 닉네임 생성
            return "카카오사용자_" + getId();
        }
        return nickname;
    }
    
    public String getNickname() {
        if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
            return kakaoAccount.getProfile().getNickname();
        }
        return properties != null ? properties.getNickname() : null;
    }
    
    public String getProfileImageUrl() {
        if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
            return kakaoAccount.getProfile().getProfileImageUrl();
        }
        return properties != null ? properties.getProfileImage() : null;
    }
}
