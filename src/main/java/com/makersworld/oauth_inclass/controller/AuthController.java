package com.makersworld.oauth_inclass.controller;

import com.makersworld.oauth_inclass.dto.AuthRequest;
import com.makersworld.oauth_inclass.dto.AuthResponse;

import com.makersworld.oauth_inclass.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 아래 메서드를 클라이언트가 호출 시, 클라이언트는 이 메서드를 호출하여 사용자를 OAuth2 인증 페이지로 보냄
    // 이후 OAuth 제공자는 사용자를 인증 후에 설정된 redirect-uri로 다시 리디렉션 
    @Operation(summary = "OAuth2 로그인 페이지로 리디렉션", description = "사용자를 지정된 OAuth2 제공자의 인증 페이지로 보냅니다. 인증 후에는 설정된 redirect-uri로 돌아옵니다.")
    // 1번 메서드 : /api/v1/auth/{provider} (사용자를 OAuth 제공자 인증 페이지로 리디렉션)
    @GetMapping("/{provider}")
    public void redirectToOAuthProvider(@PathVariable String provider, HttpServletResponse response) throws IOException {
        try {
            String url = authService.getAuthorizationUrl(provider);
            response.sendRedirect(url);
        } catch (RuntimeException e) {
            throw new RuntimeException("OAuth 제공자 '" + provider + "'로의 리디렉션에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "OAuth2 Callback", description = "Callback endpoint for OAuth2 flow. This is typically used in web-based OAuth flows.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid authentication code")
            })
    // 2번 메서드 : /api/v1/auth/login/oauth2/code/{provider} (OAuth 제공자가 사용자의 인증 결과를 담아 리다이렉트 방식으로 콜백)
    // 이 엔드포인트에서는 결과적으로 JWT 토큰을 사용자에게 응답하고 종료됨
    @GetMapping("/login/oauth2/code/{provider}")
    public ResponseEntity<AuthResponse> oauthCallback(@PathVariable String provider, @RequestParam(value = "code", required = false) String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("인증 코드가 누락되었습니다.");
        }
        try {
            AuthResponse response = authService.signInWithProvider(provider, code);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException("OAuth 제공자 '" + provider + "'를 통한 로그인에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // FE 개발자는 OAuth 인증을 직접 호출한 뒤 아래 엔드포인트를 호출해서 우리 서비스의 토큰을 요청해도 됨
    //    => 리디렉션 구조를 FE 가 수행하는 패턴
    @Operation(summary = "OAuth2 로그인", description = "Handles OAuth2 authentication by exchanging an auth code for JWT tokens.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid authentication code")
            })
    @PostMapping("/{provider}/token")
    public ResponseEntity<AuthResponse> signInWithProvider(@PathVariable String provider, @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.signInWithProvider(provider, request.getCode());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException("OAuth 제공자 '" + provider + "'를 통한 토큰 교환에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "Refresh JWT Token", description = "Generates new access and refresh tokens using a valid refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refresh successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid refresh token")
            })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException("토큰 새로고침에 실패했습니다: " + e.getMessage(), e);
        }
    }
}   