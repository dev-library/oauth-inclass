package com.makersworld.oauth_inclass.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    // JWT 설정 파일에서 값을 가져옴 (camelCase 자동 매핑)
    private long expirationMs;
    private long refreshExpiration;
} 