# OAuth 인증 시스템 아키텍처

## 프로젝트 개요

본 프로젝트는 **전략 패턴(Strategy Pattern)**을 활용하여 확장 가능한 소셜 로그인 시스템을 구현한 Spring Boot 애플리케이션입니다. 
현재 Google, Kakao, Naver 소셜 로그인을 지원하며, 새로운 OAuth 제공자를 최소한의 코드 변경으로 추가할 수 있도록 설계되었습니다.

## 🏗️ 아키텍처 설계 원칙

### 1. 책임 분리 (Separation of Concerns)

```
AuthController (프레젠테이션 계층)
    ↓
AuthService (비즈니스 로직 계층)
    ↓
OAuth2ProviderService (전략 인터페이스)
    ↓
구체적인 Provider 구현체들 (GoogleOAuth2Service, KakaoOAuth2Service, NaverOAuth2Service)
```

- **AuthController**: HTTP 요청/응답 처리 및 예외 처리
- **AuthService**: 인증 비즈니스 로직 및 사용자 관리
- **OAuth2ProviderService**: OAuth 제공자별 구현을 추상화하는 전략 인터페이스

### 2. 전략 패턴 구현

#### 전략 인터페이스
```java
public interface OAuth2ProviderService {
    String getId();                                    // 제공자 식별자
    String getAuthorizationUrl();                      // 인증 URL 생성
    String getAccessToken(String code);                // 액세스 토큰 획득
    UserInfoResponse getUserInfo(String accessToken);  // 사용자 정보 조회
}
```

#### 전략 선택 메커니즘
```java
@Service
public class AuthService {
    private final Map<String, OAuth2ProviderService> oAuth2ProviderServices;
    
    private OAuth2ProviderService getProviderService(String provider) {
        Provider.validateProvider(provider);  // Enum 검증
        OAuth2ProviderService providerService = oAuth2ProviderServices.get(provider.toLowerCase());
        if (providerService == null) {
            throw new RuntimeException("OAuth 제공자 '" + provider + "'에 대한 서비스가 구현되지 않았습니다.");
        }
        return providerService;
    }
}
```

## 🔧 확장성 설계

### 새로운 OAuth 제공자 추가 방법

새로운 소셜 로그인 제공자(예: GitHub)를 추가하려면 다음 4단계만 수행하면 됩니다:

#### 1. Provider Enum 등록
```java
// src/main/java/com/makersworld/oauth_inclass/enums/Provider.java
public enum Provider {
    GOOGLE, KAKAO, NAVER, GITHUB;  // ← GITHUB 추가
}
```

#### 2. OAuth2ProviderService 구현체 작성
```java
// src/main/java/com/makersworld/oauth_inclass/service/GitHubOAuth2Service.java
@Service
@RequiredArgsConstructor
public class GitHubOAuth2Service implements OAuth2ProviderService {
    @Override
    public String getId() {
        return "github";
    }
    
    @Override
    public String getAuthorizationUrl() {
        // GitHub OAuth 인증 URL 구현
    }
    
    @Override
    public String getAccessToken(String code) {
        // GitHub 액세스 토큰 획득 구현
    }
    
    @Override
    public UserInfoResponse getUserInfo(String accessToken) {
        // GitHub 사용자 정보 조회 구현
    }
}
```

#### 3. DTO 클래스 추가 (필요한 경우)
```java
// src/main/java/com/makersworld/oauth_inclass/dto/GitHubTokenResponse.java
// src/main/java/com/makersworld/oauth_inclass/dto/GitHubUserInfoResponse.java
```

#### 4. Properties 설정 추가
```properties
# application-config.properties
github.provider-client-id=${GITHUB_CLIENT_ID:your-github-client-id}
github.provider-client-secret=${GITHUB_CLIENT_SECRET:your-github-client-secret}

# application.properties  
spring.security.oauth2.client.registration.github.client-id=${github.provider-client-id}
spring.security.oauth2.client.registration.github.client-secret=${github.provider-client-secret}
spring.security.oauth2.client.registration.github.scope=user:email
spring.security.oauth2.client.registration.github.redirect-uri=http://localhost:8001/api/v1/auth/login/oauth2/code/github
```

### 🎯 핵심 장점

#### 1. 기존 코드 무영향
- `AuthController`와 `AuthService`의 핵심 로직은 **전혀 수정하지 않아도 됨**
- 기존 OAuth 제공자들(Google, Kakao, Naver)의 동작에 **영향 없음**

#### 2. 확장성
- 새로운 OAuth 제공자 추가 시 **4개 파일만 수정/추가**
- 제거 시에도 해당 구현체와 설정만 삭제하면 됨

#### 3. 유지보수성
- 각 OAuth 제공자별 로직이 **독립적으로 분리**되어 있음
- 한 제공자의 API 변경이 다른 제공자에게 **영향을 주지 않음**

#### 4. 테스트 용이성
- 각 OAuth 제공자별로 **독립적인 단위 테스트** 가능
- Mock 객체를 이용한 테스트 시 **특정 제공자만 격리 테스트** 가능

## 📁 프로젝트 구조

```
src/main/java/com/makersworld/oauth_inclass/
├── controller/
│   ├── AuthController.java           # OAuth 인증 엔드포인트
│   ├── UserProfileController.java    # 사용자 프로필 관리
│   └── JwkController.java           # JWT 공개키 엔드포인트
├── service/
│   ├── AuthService.java             # 인증 비즈니스 로직
│   ├── OAuth2ProviderService.java   # 전략 패턴 인터페이스
│   ├── GoogleOAuth2Service.java     # Google OAuth 구현체
│   ├── KakaoOAuth2Service.java      # Kakao OAuth 구현체
│   ├── NaverOAuth2Service.java      # Naver OAuth 구현체
│   ├── JwtService.java              # JWT 토큰 관리
│   └── UserService.java             # 사용자 관리
├── dto/
│   ├── AuthRequest.java             # 인증 요청 DTO
│   ├── AuthResponse.java            # 인증 응답 DTO
│   ├── UserInfoResponse.java        # 통합 사용자 정보 DTO
│   ├── GoogleTokenResponse.java     # Google 토큰 응답 DTO
│   ├── GoogleUserInfoResponse.java  # Google 사용자 정보 DTO
│   ├── KakaoTokenResponse.java      # Kakao 토큰 응답 DTO
│   ├── KakaoUserInfoResponse.java   # Kakao 사용자 정보 DTO
│   ├── NaverTokenResponse.java      # Naver 토큰 응답 DTO
│   └── NaverUserInfoResponse.java   # Naver 사용자 정보 DTO
├── enums/
│   ├── Provider.java                # OAuth 제공자 열거형
│   └── Role.java                    # 사용자 권한 열거형
├── model/
│   ├── User.java                    # 사용자 엔티티
│   └── UserProfile.java            # 사용자 프로필 엔티티
└── config/
    ├── SecurityConfig.java          # Spring Security 설정
    ├── JwtProperties.java           # JWT 설정 프로퍼티
    └── AppConfig.java               # 애플리케이션 설정
```

## 🔐 보안 설정

### 설정 파일 분리
- **application.properties**: 공개 설정 (포트, 데이터베이스, JWT 만료시간 등)
- **application-config.properties**: 민감한 OAuth 클라이언트 정보 (gitignore 적용)

### Profile 기반 설정 관리
```properties
# application.properties
spring.profiles.include=config

# OAuth 클라이언트 ID/Secret은 config profile에서 참조
spring.security.oauth2.client.registration.google.client-id=${google.provider-client-id}
spring.security.oauth2.client.registration.google.client-secret=${google.provider-client-secret}
```

## 🚀 API 엔드포인트

### 인증 관련 엔드포인트
- `GET /api/v1/auth/{provider}` - OAuth 제공자 인증 페이지로 리다이렉트
- `GET /api/v1/auth/login/oauth2/code/{provider}` - OAuth 콜백 처리
- `POST /api/v1/auth/{provider}/token` - 인증 코드로 JWT 토큰 교환
- `POST /api/v1/auth/refresh` - 리프레시 토큰으로 액세스 토큰 갱신

### 지원하는 OAuth 제공자
- `google` - Google OAuth 2.0
- `kakao` - Kakao OAuth 2.0  
- `naver` - Naver OAuth 2.0

## 🧪 사용 예시

### 1. Google 로그인 플로우
```bash
# 1. 사용자를 Google 인증 페이지로 리다이렉트
GET /api/v1/auth/google

# 2. Google에서 콜백으로 인증 코드 전달 (자동)
GET /api/v1/auth/login/oauth2/code/google?code=AUTHORIZATION_CODE

# 3. 또는 프론트엔드에서 직접 토큰 교환
POST /api/v1/auth/google/token
{
  "code": "AUTHORIZATION_CODE"
}
```

### 2. 토큰 갱신
```bash
POST /api/v1/auth/refresh?refreshToken=REFRESH_TOKEN
```

## 🔄 확장 시나리오

### 시나리오 1: Discord OAuth 추가
1. `Provider.java`에 `DISCORD` 추가
2. `DiscordOAuth2Service.java` 구현
3. Discord 관련 DTO 클래스 추가
4. `application-config.properties`에 Discord 설정 추가

**결과**: 기존 Google, Kakao, Naver 로그인은 전혀 영향받지 않음

### 시나리오 2: Kakao 로그인 제거
1. `KakaoOAuth2Service.java` 삭제
2. Kakao 관련 DTO 삭제
3. `Provider.java`에서 `KAKAO` 제거
4. Properties에서 Kakao 설정 제거

**결과**: Google, Naver 로그인은 정상 동작 유지

## 📈 성능 및 모니터링

### 전략 패턴의 성능 이점
- **런타임에 전략 선택**: Map 기반 O(1) 조회
- **메모리 효율성**: 사용하지 않는 제공자 서비스도 미리 로드되지만 경량 객체
- **확장성**: 제공자 수가 증가해도 성능 저하 없음

## 🛠️ 개발 및 배포

### 로컬 개발 환경 설정
1. MySQL 데이터베이스 준비 (포트: 4406)
2. `application-config.properties` 파일 생성 및 OAuth 클라이언트 정보 입력
3. 각 OAuth 제공자에서 콜백 URL 등록:
   - Google: `http://localhost:8001/api/v1/auth/login/oauth2/code/google`
   - Kakao: `http://localhost:8001/api/v1/auth/login/oauth2/code/kakao`
   - Naver: `http://localhost:8001/api/v1/auth/login/oauth2/code/naver`

### 실행
```bash
./gradlew bootRun
```

### Swagger UI 접속
- URL: http://localhost:8001/swagger-ui.html
- API 문서: http://localhost:8001/v3/api-docs

## 📚 결론

본 프로젝트는 **전략 패턴을 통한 확장 가능한 OAuth 시스템**의 모범 사례를 보여줍니다. 
새로운 소셜 로그인 제공자를 추가하거나 기존 제공자를 수정/제거할 때, 다른 컴포넌트에 미치는 영향을 최소화하면서도 
코드의 가독성과 유지보수성을 극대화할 수 있습니다.

이러한 설계를 통해 비즈니스 요구사항의 변화에 빠르게 대응할 수 있으며, 
각 OAuth 제공자별 특성을 독립적으로 처리할 수 있는 유연한 시스템을 구축했습니다.
