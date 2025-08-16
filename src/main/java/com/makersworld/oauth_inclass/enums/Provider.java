package com.makersworld.oauth_inclass.enums;

public enum Provider {
    GOOGLE, KAKAO, NAVER;

    /**
     * 문자열이 유효한 Provider인지 검증합니다.
     * @param provider 검증할 provider 문자열
     * @throws RuntimeException 지원하지 않는 provider인 경우
     */
    public static void validateProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new RuntimeException("Provider가 비어있습니다.");
        }
        
        try {
            valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
    }

    /**
     * 문자열이 유효한 Provider인지 확인합니다.
     * @param provider 확인할 provider 문자열
     * @return 유효한 provider이면 true, 아니면 false
     */
    public static boolean isValidProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            return false;
        }
        
        try {
            valueOf(provider.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 