package com.makersworld.oauth_inclass.service;

import com.makersworld.oauth_inclass.dto.KakaoTokenResponse;
import com.makersworld.oauth_inclass.dto.KakaoUserInfoResponse;
import com.makersworld.oauth_inclass.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service("kakao")
@RequiredArgsConstructor
public class KakaoOAuth2Service implements OAuth2ProviderService {

    // application.properties에 설정된 값들을 주입받음
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient; // 비동기 HTTP 통신을 위한 클라이언트

    @Override
    public String getId() {
        return "kakao";
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code";
    }

    // 1. 인증 코드로 Kakao에 액세스 토큰 요청
    @Override
    public String getAccessToken(String code) {
        String tokenUri = "https://kauth.kakao.com/oauth/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");  // 권한 유형 (인증 코드 교환)
        params.add("client_id", clientId);               // 클라이언트 ID (Kakao 앱 설정에서 발급받은 값 -> 백엔드에서 관리)
        params.add("client_secret", clientSecret);       // 클라이언트 시크릿 (Kakao 앱 설정에서 발급받은 값 -> 백엔드에서 관리)
        params.add("redirect_uri", redirectUri);         // 리다이렉트 URI (Kakao 앱 설정에서 발급받은 값 -> 백엔드에서 구현)
        params.add("code", code);                        // 인증 코드 (사용자 브라우저에서 수신)

        KakaoTokenResponse response = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve() // 응답을 받아옴
                .bodyToMono(KakaoTokenResponse.class) // 응답 본문을 KakaoTokenResponse 객체로 변환
                .block(); // 비동기 작업이 끝날 때까지 대기

        if (response == null) {
            throw new RuntimeException("Failed to get access token from Kakao");
        }
        return response.getAccessToken();
    }

    // 2. 액세스 토큰으로 Kakao에 사용자 정보 요청
    @Override
    public UserInfoResponse getUserInfo(String accessToken) {
        String userInfoUri = "https://kapi.kakao.com/v2/user/me";

        KakaoUserInfoResponse kakaoResponse = webClient.get()
                .uri(userInfoUri)
                .headers(headers -> headers.setBearerAuth(accessToken)) // 헤더에 Bearer 토큰 추가
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .block();

        if (kakaoResponse == null) {
            throw new RuntimeException("Failed to get user info from Kakao");
        }

        // 디버깅용 로그 추가
        System.out.println("=== Kakao API Response Debug ===");
        System.out.println("ID: " + kakaoResponse.getId());
        System.out.println("Raw Nickname: " + kakaoResponse.getNickname());
        System.out.println("Validated Nickname: " + kakaoResponse.getValidatedNickname());
        System.out.println("Raw Email: " + kakaoResponse.getEmail());
        System.out.println("Validated Email: " + kakaoResponse.getValidatedEmail());
        System.out.println("Profile Image URL: " + kakaoResponse.getProfileImageUrl());
        System.out.println("KakaoAccount: " + (kakaoResponse.getKakaoAccount() != null ? "있음" : "없음"));
        if (kakaoResponse.getKakaoAccount() != null) {
            System.out.println("Profile: " + (kakaoResponse.getKakaoAccount().getProfile() != null ? "있음" : "없음"));
            if (kakaoResponse.getKakaoAccount().getProfile() != null) {
                System.out.println("Profile Nickname: " + kakaoResponse.getKakaoAccount().getProfile().getNickname());
            }
        }
        System.out.println("Properties: " + (kakaoResponse.getProperties() != null ? "있음" : "없음"));
        if (kakaoResponse.getProperties() != null) {
            System.out.println("Properties Nickname: " + kakaoResponse.getProperties().getNickname());
        }
        System.out.println("================================");

        // KakaoUserInfoResponse를 범용 UserInfoResponse로 변환
        // getValidatedEmail()과 getValidatedNickname() 사용하여 null 값 방지
        return UserInfoResponse.builder()
                .id(String.valueOf(kakaoResponse.getId())) // Long을 String으로 변환
                .email(kakaoResponse.getValidatedEmail()) // 검증된 이메일 사용
                .name(kakaoResponse.getValidatedNickname()) // 검증된 닉네임 사용 (profile_nickname)
                .picture(kakaoResponse.getProfileImageUrl())
                .build();
    }
}
