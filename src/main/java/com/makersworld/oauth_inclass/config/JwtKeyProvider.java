package com.makersworld.oauth_inclass.config;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * JWT 토큰 서명 및 검증을 위한 RSA 키 쌍을 관리하는 컴포넌트
 * 비대칭 암호화 방식을 사용하여 보안성을 향상시킵니다.
 */
@Component
public class JwtKeyProvider {

    private KeyPair keyPair;

    /**
     * 애플리케이션 초기화 시 RSA 키 쌍을 생성합니다.
     * 실제 운영 환경에서는 키가 애플리케이션에서 생성되어서는 안되고
     * 안전하게 일정한 위치(DB, Secret Manager Vault 등)에서 관리되며
     * 앱 구동시 외부에서 주입받아야 합니다.
     */
    @PostConstruct
    public void init() {  // ssh-keygen -t rsa -b 4096 -m PEM -f jwtRS256.key 과 같은 방식으로 외부에서 수행해야 함
        // RSA256 알고리즘을 사용하여 키 쌍 생성 (최신 API 사용)
        this.keyPair = Jwts.SIG.RS256.keyPair().build();

        // 향후 외부에서 주입받는 방식 예제 (도커라이징 및 코드 배포 시 외부에서 주입받는 방식)
        // this.keyPair = KeyPair.load(new File("jwtRS256.key"));
    }

    /**
     * JWT 토큰 서명에 사용할 개인키를 반환합니다.
     * @return RSA 개인키
     */
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    /**
     * JWT 토큰 검증에 사용할 공개키를 반환합니다.
     * @return RSA 공개키
     */
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}