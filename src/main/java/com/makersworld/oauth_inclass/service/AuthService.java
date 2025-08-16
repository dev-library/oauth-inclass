package com.makersworld.oauth_inclass.service;

import com.makersworld.oauth_inclass.dto.AuthResponse;
import com.makersworld.oauth_inclass.dto.UserInfoResponse;
import com.makersworld.oauth_inclass.enums.Provider;
import com.makersworld.oauth_inclass.enums.Role;
import com.makersworld.oauth_inclass.model.User;
import com.makersworld.oauth_inclass.model.UserProfile;
import com.makersworld.oauth_inclass.repository.UserRepository;
import com.makersworld.oauth_inclass.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    
    // OAuth2ProviderService 구현체들을 Map으로 관리 (전략 패턴)
    private final Map<String, OAuth2ProviderService> oAuth2ProviderServices;

    // 생성자에서 OAuth2ProviderService 구현체들을 Map으로 변환
    /*
    public AuthService(UserRepository userRepository, 
                     UserProfileRepository userProfileRepository,
                     JwtService jwtService,
                     List<OAuth2ProviderService> oAuth2ProviderServices) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.oAuth2ProviderServices = oAuth2ProviderServices.stream()
                .collect(Collectors.toMap(OAuth2ProviderService::getId, Function.identity()));
    }
     */

    // AuthController에서 호출하는 새로운 메서드들
    public String getAuthorizationUrl(String provider) {
        OAuth2ProviderService providerService = getProviderService(provider);
        return providerService.getAuthorizationUrl();
    }

    // 기존 호환성을 위한 메서드 (deprecated)
    @Deprecated
    public String getGoogleAuthorizationUrl() {
        return getAuthorizationUrl("google");
    }

    private OAuth2ProviderService getProviderService(String provider) {
        // Provider Enum 검증 후 서비스 조회
        Provider.validateProvider(provider);
        OAuth2ProviderService providerService = oAuth2ProviderServices.get(provider.toLowerCase());
        if (providerService == null) {
            throw new RuntimeException("OAuth 제공자 '" + provider + "'에 대한 서비스가 구현되지 않았습니다.");
        }
        return providerService;
    }

    @Transactional
    public AuthResponse signInWithProvider(String provider, String code) {
        OAuth2ProviderService providerService = getProviderService(provider);
        
        // 1. OAuth 제공자에서 사용자 정보 가져오기
        String accessToken = providerService.getAccessToken(code);
        UserInfoResponse userInfo = providerService.getUserInfo(accessToken);

        // 2. 사용자 정보 DB와 동기화
        boolean isNewUser = userRepository.findByEmail(userInfo.getEmail()).isEmpty();
        Provider providerEnum = getProviderEnum(provider);

        User user = userRepository.findByEmail(userInfo.getEmail())
               .map(existingUser -> {
                    // 기존 유저: 이름 업데이트
                    existingUser.updateName(userInfo.getName());
                    return userRepository.save(existingUser);
                })
               .orElseGet(() -> {
                    // 신규 유저: DB에 저장
                    return userRepository.save(User.builder()
                           .email(userInfo.getEmail())
                           .name(userInfo.getName())
                           .provider(providerEnum)
                           .providerId(userInfo.getId())
                           .role(Role.USER)
                           .build());
                });

        // 3. 신규 유저일 경우, 프로필 사진으로 프로필 자동 생성
        if (isNewUser) {
            createUserProfile(user, userInfo);
        }

        // 4. JWT 토큰 생성 및 응답
        String accessTokenJwt = jwtService.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessTokenJwt, refreshToken, "Bearer", 86400L,
                user.getEmail(), user.getName(), user.getRole()
        );
        // 이후 토큰 검사하는 부분은 JWT 필터 & 사이트 보안 전용 모듈(Spring Security)에서 처리함
        // middleware, interceptor 등의 표현으로 Controller 이전 단계에서 우리 서비스를 위한 처리 준비를 수행
    }

    // 기존 호환성을 위한 메서드 (deprecated)
    @Deprecated
    @Transactional
    public AuthResponse signInWithGoogle(String code) {
        return signInWithProvider("google", code);
    }

    private Provider getProviderEnum(String provider) {
        // Provider Enum의 검증 메서드 사용
        Provider.validateProvider(provider);
        return Provider.valueOf(provider.toUpperCase());
    }

    private void createUserProfile(User user, UserInfoResponse userInfo) {
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .avatarUrl(userInfo.getPicture())
                .build();
        
        userProfileRepository.save(userProfile);
    }

    // 기존 호환성을 위한 메서드 (deprecated)
    @Deprecated
    private void createUserProfileFromGoogle(User user, com.makersworld.oauth_inclass.dto.GoogleUserInfoResponse userInfo) {
        UserInfoResponse genericUserInfo = UserInfoResponse.builder()
                .id(userInfo.getId())
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .picture(userInfo.getPicture())
                .build();
        createUserProfile(user, genericUserInfo);
    }

    public AuthResponse refreshToken(String refreshToken) {
        // 1. 리프레시 토큰에서 이메일 추출
        String email = jwtService.extractEmail(refreshToken);
        
        // 2. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 3. 리프레시 토큰 유효성 검증
        if (!jwtService.validateToken(refreshToken, email)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }
        
        // 4. 새로운 액세스 토큰과 리프레시 토큰 생성
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        return new AuthResponse(
                newAccessToken, newRefreshToken, "Bearer", 86400L,
                user.getEmail(), user.getName(), user.getRole()
        );
    }
}
