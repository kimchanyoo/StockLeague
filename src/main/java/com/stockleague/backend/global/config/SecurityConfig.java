package com.stockleague.backend.global.config;

import com.stockleague.backend.auth.jwt.JwtAuthenticationFilter;
import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.global.handler.CustomAccessDeniedHandler;
import com.stockleague.backend.infra.properties.CorsProperties;
import com.stockleague.backend.infra.redis.TokenRedisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CorsProperties corsProperties;
    private final TokenRedisService redisService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, redisService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAccessDeniedHandler customAccessDeniedHandler)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/api/v1/auth/**",
                                "/api/v1/notices/**",
                                "/error",
                                "/api/v1/stocks/*/comments",
                                "/api/v1/comments/*/replies",
                                "/api/v1/stocks",
                                "/api/v1/stocks/popular",
                                "/api/v1/stocks/search",
                                "/api/v1/stocks/*/candles",
                                "/api/v1/stocks/*/price",
                                "/api/v1/stocks/*/orderbook",
                                "/api/v1/openapi/**",
                                "/ws/**",
                                "/ws-sockjs/**",
                                "/ws-debug/active-users"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // 관리자 전용 URL
                        .anyRequest().authenticated() // 나머지는 로그인한 사용자만 접근 가능
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 슬래시 제거된 origin 리스트로 정제
        List<String> origins = corsProperties.getAllowedOrigins().stream()
                .map(origin -> origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin)
                .toList();

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
