package com.academy.creator_hub.config.web;

import com.academy.creator_hub.domain.youtube.service.SparkRecommendationService;
import com.academy.creator_hub.jwt.JwtUtil;
import com.academy.creator_hub.domain.auth.repository.UserRepository;
import com.academy.creator_hub.security.JwtAuthenticationFilter;
import com.academy.creator_hub.security.JwtAuthorizationFilter;
import com.academy.creator_hub.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final SparkRecommendationService sparkRecommendationService;

    public WebSecurityConfig(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, UserRepository userRepository, SparkRecommendationService sparkRecommendationService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.sparkRecommendationService = sparkRecommendationService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()  // CSRF 보호 비활성화 (필요시 활성화)
                .authorizeRequests()
                .antMatchers("/css/**", "/images/**", "/main", "/signup", "/login", "/popular", "/search", "/video/**", "/keywords/trend", "/**").permitAll()
                .anyRequest().authenticated();  // 나머지 경로는 인증이 필요

        // JWT 인증 및 인가 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userRepository, sparkRecommendationService);
        filter.setAuthenticationManager(authenticationManager());
        filter.setFilterProcessesUrl("/login");  // 로그인 경로 설정
        return filter;
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


