package com.academy.creator_hub.jwt;

import com.academy.creator_hub.dto.TokenResponseDto;
import com.academy.creator_hub.model.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.ExpiredJwtException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;


@Component
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "RefreshToken";
    public static final String AUTHORIZATION_KEY = "auth";
    public static final String BEARER_PREFIX = "Bearer ";
    private final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L; // 60분
    private final long REFRESH_TOKEN_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes); // Base64로 디코딩하여 키 초기화
    }

    // 토큰 생성 메서드
    public TokenResponseDto createToken(String username, UserRoleEnum role) {
        Date date = new Date();

        String accessToken = BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();

        String refreshToken = BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();

        return new TokenResponseDto(accessToken, refreshToken);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            logger.error("SecurityException: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        } catch (MalformedJwtException e) {
            logger.error("MalformedJwtException: {}", e.getMessage());
            throw new IllegalArgumentException("잘못된 형식의 토큰입니다.");
        } catch (SignatureException e) {
            logger.error("SignatureException: {}", e.getMessage());
            throw new IllegalArgumentException("서명이 유효하지 않은 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.error("UnsupportedJwtException: {}", e.getMessage());
            throw new IllegalArgumentException("지원되지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException: {}", e.getMessage());
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        } catch (ExpiredJwtException e) {
            logger.error("ExpiredJwtException: {}", e.getMessage());
            throw new IllegalArgumentException("토큰이 만료되었습니다.");
        }
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // header 에서 JWT 가져오기
    public String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length()).trim(); // 공백 제거
        }
        throw new IllegalArgumentException("토큰이 헤더에 존재하지 않습니다.");
    }

    public String getTokenFromRequest(HttpServletRequest req) {
        return req.getHeader(AUTHORIZATION_HEADER);
    }

    public String substringToken(String token) {
        return token.substring(BEARER_PREFIX.length()).trim(); // "Bearer " 접두사 제거 후 공백 제거
    }

    // JWT 토큰에서 권한 가져오기
    public UserRoleEnum getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key) // 키 사용
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UserRoleEnum.valueOf(claims.get(AUTHORIZATION_KEY).toString());
    }

    // JWT 토큰이 만료되었는지 확인
    public Boolean isTokenExpired(String token) {
        Claims claims = getUserInfoFromToken(token);
        Date date = claims.getExpiration();
        return date.before(new Date());
    }

    public String getRefreshTokenFromHeader(HttpServletRequest request) {
        String refreshToken = request.getHeader(REFRESH_TOKEN_HEADER);
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("리프레쉬 토큰을 헤더에서 찾을 수 없습니다.");
        }
        return substringToken(refreshToken);
    }
}
