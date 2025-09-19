package com.example.backend.common.config;

import com.example.backend.jwt.filter.JwtAuthenticationFilter;
import com.example.backend.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final JwtProvider jwtProvider;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/.well-known/**", // ✅ App Links(assetlinks.json) 허용
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/user/register",
                                "/user/login",
                                "/schedule/public/**",
                                "/user/password",
                                "/mail/**",
                                "/health/**",
                                // 투어 관련 API는 인증 없이 가능
                                "/tour/search",
                                "/tour/detail/**",
                                "/tour/search/theme",
                                "/places/**",
                                "/regions/**",
                                "/file/**",
                                "/auth/**",
                                "/schedule/**",
                                "/weather/**",
                                "/comment/**",
                                "/board/**",
                                "/schedule/**"

                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://yeodam.site",
                "http://yeodam.site",
                "https://yeodam.vercel.app",
                "capacitor://localhost",
                "http://localhost",        // ✅ 포트 없는 localhost
                "http://localhost:*"       // ✅ 임의 포트 localhost (Capacitor가 종종 사용)
                // 필요 시 에뮬레이터: "http://10.0.2.2:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}