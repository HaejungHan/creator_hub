package com.academy.creator_hub.security;

import com.academy.creator_hub.dto.LoginRequestDto;
import com.academy.creator_hub.dto.TokenResponseDto;
import com.academy.creator_hub.model.UserRoleEnum;
import com.academy.creator_hub.jwt.JwtUtil;
import com.academy.creator_hub.repository.UserRepository;
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

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        setFilterProcessesUrl("/api/login");
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

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, tokenResponse.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN_HEADER, tokenResponse.getRefreshToken());

        // 리프래쉬 토큰 유저 DB에 추가
        userRepository.findByUsername(username).ifPresent(
                user -> {
                    user.updateRefresh(tokenResponse.getRefreshToken());
                    userRepository.save(user);
                }
        );

        responseBody(response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.info("로그인 실패");
        response.setStatus(401);
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
