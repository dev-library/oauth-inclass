package com.makersworld.oauth_inclass.config;

import com.makersworld.oauth_inclass.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 보안설정을 룰 기반이 아니라 순차 처리 코드 방식으로 수행해서
        // 디테일한 제어가 가능함 VS 추상화 레벨은 매우 낮음
        http
           .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
           .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (Stateless API)
           .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 인증 비활성화
           .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 비활성화
           // 세션을 사용하지 않는 Stateless 정책 설정
           .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           // 경로별 접근 권한 설정
           /*
            * ---- 아래 체이닝은 Whitelist 방식으로 접근 권한을 설정하는 방식 ----
            * 1. 인증 관련 API는 모두 허용
            * 2. 프로필 API는 인증 필요 (사실 명시 불필요)
            * 3. 나머지 모든 요청은 인증 필요
            */
           .authorizeHttpRequests(auth -> auth
               // 인증 관련 API는 모두 허용
               .requestMatchers(
                   "/.well-known/jwks.json",
                   "/api/v1/auth/**",
                   "/error",
                   "/swagger-ui/**", // Swagger UI 접근은 개발 환경에서만 허용
                   "/swagger-ui.html",
                   "/v3/api-docs/**"
               ).permitAll()
               // 프로필 API는 인증 필요
               .requestMatchers("/api/v1/profile/**").authenticated() 
               // 나머지 모든 요청은 인증 필요
               .anyRequest().authenticated() 
            )
           // UsernamePasswordAuthenticationFilter 앞에 우리 커스텀 필터 추가
           .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS(Cross-Origin Resource Sharing) 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 개발 서버(React) 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("<http://localhost:9002>", "<http://localhost:5173>", "<http://localhost:3000>"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
