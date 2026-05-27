package com.example.OSSP_BackEnd.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 허용될 경로들
    private static final String[] AUTH_WHITELIST = {
        "/api/v1/auth/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (Stateless API 서버이므로)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS 설정 추가
            .cors(Customizer.withDefaults())
            
            // 세션 관리 정책을 STATELESS로 설정
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // HTTP 요청에 대한 접근 권한 설정
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AUTH_WHITELIST).permitAll() // 허용 목록에 있는 경로는 인증 없이 접근 허용
                .anyRequest().authenticated() // 나머지 모든 경로는 인증 필요
            )
            
            // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈을 등록합니다.
     * BCrypt 알고리즘을 사용합니다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security의 인증을 총괄하는 AuthenticationManager 빈을 등록합니다.
     * @param authenticationConfiguration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}