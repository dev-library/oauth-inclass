package com.makersworld.oauth_inclass.service;

import com.makersworld.oauth_inclass.dto.NaverTokenResponse;
import com.makersworld.oauth_inclass.dto.NaverUserInfoResponse;
import com.makersworld.oauth_inclass.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service("naver")
@RequiredArgsConstructor
public class NaverOAuth2Service implements OAuth2ProviderService {

    // application.properties에 설정된 값들을 주입받음
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient; // 비동기 HTTP 통신을 위한 클라이언트

    @Override
    public String getId() {
        return "naver";
    }

    @Override
    public String getAuthorizationUrl() {
        // 네이버는 state 파라미터가 필수
        String state = UUID.randomUUID().toString();
        return "https://nid.naver.com/oauth2.0/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;
    }

    // 1. 인증 코드로 Naver에 액세스 토큰 요청
    @Override
    public String getAccessToken(String code) {
        String tokenUri = "https://nid.naver.com/oauth2.0/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");  // 권한 유형 (인증 코드 교환)
        params.add("client_id", clientId);               // 클라이언트 ID (Naver 앱 설정에서 발급받은 값 -> 백엔드에서 관리)
        params.add("client_secret", clientSecret);       // 클라이언트 시크릿 (Naver 앱 설정에서 발급받은 값 -> 백엔드에서 관리)
        params.add("code", code);                        // 인증 코드 (사용자 브라우저에서 수신)
        params.add("state", UUID.randomUUID().toString()); // state 파라미터 (CSRF 방지)

        NaverTokenResponse response = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve() // 응답을 받아옴
                .bodyToMono(NaverTokenResponse.class) // 응답 본문을 NaverTokenResponse 객체로 변환
                .block(); // 비동기 작업이 끝날 때까지 대기

        if (response == null || response.getAccessToken() == null) {
            throw new RuntimeException("Failed to get access token from Naver");
        }
        return response.getAccessToken();
    }

    // 2. 액세스 토큰으로 Naver에 사용자 정보 요청
    @Override
    public UserInfoResponse getUserInfo(String accessToken) {
        String userInfoUri = "https://openapi.naver.com/v1/nid/me";

        NaverUserInfoResponse naverResponse = webClient.get()
                .uri(userInfoUri)
                .headers(headers -> headers.setBearerAuth(accessToken)) // 헤더에 Bearer 토큰 추가
                .retrieve()
                .bodyToMono(NaverUserInfoResponse.class)
                .block();

        if (naverResponse == null || !"00".equals(naverResponse.getResultcode())) {
            throw new RuntimeException("Failed to get user info from Naver");
        }

        // 디버깅용 로그 추가
        System.out.println("=== Naver API Response Debug ===");
        System.out.println("Resultcode: " + naverResponse.getResultcode());
        System.out.println("Message: " + naverResponse.getMessage());
        System.out.println("ID: " + naverResponse.getId());
        System.out.println("Raw Name: " + naverResponse.getName());
        System.out.println("Raw Nickname: " + naverResponse.getNickname());
        System.out.println("Validated Name: " + naverResponse.getValidatedName());
        System.out.println("Raw Email: " + naverResponse.getEmail());
        System.out.println("Validated Email: " + naverResponse.getValidatedEmail());
        System.out.println("Profile Image: " + naverResponse.getProfileImage());
        System.out.println("================================");

        // NaverUserInfoResponse를 범용 UserInfoResponse로 변환
        // getValidatedEmail()과 getValidatedName() 사용하여 null 값 방지
        return UserInfoResponse.builder()
                .id(naverResponse.getId())
                .email(naverResponse.getValidatedEmail()) // 검증된 이메일 사용
                .name(naverResponse.getValidatedName())   // 검증된 이름 사용 (name → nickname → 기본값 순서)
                .picture(naverResponse.getProfileImage())
                .build();
    }
}
