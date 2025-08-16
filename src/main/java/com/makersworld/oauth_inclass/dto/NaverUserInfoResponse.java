package com.makersworld.oauth_inclass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverUserInfoResponse {
    
    @JsonProperty("resultcode")
    private String resultcode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("response")
    private Response response;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("nickname")
        private String nickname;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("gender")
        private String gender;
        
        @JsonProperty("age")
        private String age;
        
        @JsonProperty("birthday")
        private String birthday;
        
        @JsonProperty("profile_image")
        private String profileImage;
        
        @JsonProperty("birthyear")
        private String birthyear;
        
        @JsonProperty("mobile")
        private String mobile;
    }
    
    // 편의 메서드들
    public String getId() {
        return response != null ? response.getId() : null;
    }
    
    public String getEmail() {
        return response != null ? response.getEmail() : null;
    }
    
    public String getName() {
        return response != null ? response.getName() : null;
    }
    
    public String getNickname() {
        return response != null ? response.getNickname() : null;
    }
    
    public String getProfileImage() {
        return response != null ? response.getProfileImage() : null;
    }
    
    /**
     * 이메일 검증 및 기본값 제공 메서드
     * 네이버에서 이메일이 제공되지 않는 경우 더미 이메일 반환
     * 
     * TODO: 네이버에서 이메일 수집이 안정화되면 이 메서드 검토 필요
     * 
     * @return 유효한 이메일 또는 더미 이메일
     */
    public String getValidatedEmail() {
        String email = getEmail();
        if (email == null || email.trim().isEmpty()) {
            // 네이버에서 이메일을 제공하지 않는 경우 더미 이메일 생성
            // 사용자 ID를 포함하여 고유성 보장
            return "naver_user_" + getId() + "@dummy.naver.local";
        }
        return email;
    }
    
    /**
     * 이름 검증 및 기본값 제공 메서드
     * 네이버에서 이름이 제공되지 않는 경우 닉네임 또는 기본 이름 반환
     * 
     * TODO: 향후 이름 수집이 안정화되면 이 메서드 검토 필요
     * 
     * @return 유효한 이름 또는 기본 이름
     */
    public String getValidatedName() {
        String name = getName();
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        
        String nickname = getNickname();
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        
        // 이름과 닉네임 모두 없는 경우 기본 이름 생성
        return "네이버사용자_" + getId();
    }
}
