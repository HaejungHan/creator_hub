package com.academy.creator_hub.security;

import com.academy.creator_hub.domain.auth.dto.LoginRequestDto;
import com.academy.creator_hub.domain.auth.dto.TokenResponseDto;
import com.academy.creator_hub.domain.auth.model.UserRoleEnum;
import com.academy.creator_hub.domain.youtube.service.SparkRecommendationService;
import com.academy.creator_hub.jwt.JwtUtil;
import com.academy.creator_hub.domain.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.SerializablePermission;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final SparkRecommendationService sparkRecommendationService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository, SparkRecommendationService sparkRecommendationService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.sparkRecommendationService = sparkRecommendationService;
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("료그인 시도");
        try {
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        log.info("로그인 성공 및 JWT 생성");

        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();
        TokenResponseDto tokenResponse = jwtUtil.createToken(username, role);

        response.addHeader("Set-Cookie", "access_token=" + tokenResponse.getAccessToken() + "; path=/; Secure; SameSite=Strict");
        response.addHeader("Set-Cookie", "refresh_token=" + tokenResponse.getRefreshToken() + "; path=/; Secure; SameSite=Strict");

        userRepository.findByUsername(username).ifPresent(
                user -> {
                    user.updateRefresh(tokenResponse.getRefreshToken());
                    userRepository.save(user);
                }
        );

        sparkRecommendationService.generateRecommendations(username);
        responseBody(response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.info("로그인 실패");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 Unauthorized
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"로그인 실패: 아이디나 비밀번호를 확인해주세요.\"}");
    }

    private String responseBody(HttpServletResponse response) {
        String responseMessage = "로그인이 완료되었습니다.";

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(responseMessage);
            response.getWriter().write(jsonResponse);
        } catch (IOException e) {
            log.error("응답 작성 중 오류 발생: {}", e.getMessage());
        }
        return responseMessage;
    }

}
