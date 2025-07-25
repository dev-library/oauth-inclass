package com.makersworld.oauth_inclass.service;

import com.makersworld.oauth_inclass.dto.GoogleTokenResponse;
import com.makersworld.oauth_inclass.dto.GoogleUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    // application.properties에 설정된 값들을 주입받음
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private String scope;

    private final WebClient webClient; // 비동기 HTTP 통신을 위한 클라이언트

    public String getGoogleAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scope.replace(",", " ") +
                "&access_type=offline";
    }

    // 1. 인증 코드로 Google에 액세스 토큰 요청
    public String getAccessToken(String code) {
        String tokenUri = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);                        // 인증 코드 (사용자 브라우저에서 수신)
        params.add("client_id", clientId);               // 클라이언트 ID (Google 클라이언트 설정에서 발급받은 값 -> 백엔드에서 관리)
        params.add("client_secret", clientSecret);       // 클라이언트 시크릿 (Google 클라이언트 설정에서 발급받은 값 -> 백엔드에서 관리)
        params.add("redirect_uri", redirectUri);         // 리다이렉트 URI (Google 클라이언트 설정에서 발급받은 값 -> 백엔드에서 구현)
        params.add("grant_type", "authorization_code");  // 권한 유형 (인증 코드 교환)

        GoogleTokenResponse response = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve() // 응답을 받아옴
                .bodyToMono(GoogleTokenResponse.class) // 응답 본문을 GoogleTokenResponse 객체로 변환
                .block(); // 비동기 작업이 끝날 때까지 대기
                // 향후 block 대신 비동기 처리를 위해 Mono를 사용할 수 있음
                // Mono 객체는 promise, future 객체와 유사한 비동기 처리 기능을 제공함
                // 나중에, 미래에 blocking 방식으로 데이터를 요청할 수 있는 요청 포인트 객체로 사용됨

        if (response == null) {
            throw new RuntimeException("Failed to get access token from Google");
        }
        return response.getAccessToken();
    }

    // 2. 액세스 토큰으로 Google에 사용자 정보 요청
    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        String userInfoUri = "https://www.googleapis.com/oauth2/v2/userinfo";

        GoogleUserInfoResponse response = webClient.get()
                .uri(userInfoUri)
                .headers(headers -> headers.setBearerAuth(accessToken)) // 헤더에 Bearer 토큰 추가
                .retrieve()
                .bodyToMono(GoogleUserInfoResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Failed to get user info from Google");
        }
        return response;
    }
}
