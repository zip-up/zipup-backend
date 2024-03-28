package com.zipup.server.global.security.util;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.user.dto.TokenResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zipup.server.global.exception.CustomErrorCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class JwtProvider {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.jwt.secret}")
    private String key;

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    @Value("${spring.jwt.header}")
    private String accessHeader;

    @Value("${spring.jwt.prefix}")
    private String prefix;

    private final String refresh = "_REFRESH";

    private Key secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenResponse generateToken(String id, String role) {
        Claims claims = Jwts.claims().setId(id);
        claims.put("role", role);

        String accessToken = generateAccessToken(claims);
        String refreshToken = generateRefreshToken(claims);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String generateAccessToken(Claims claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        redisTemplate.opsForValue().set(
                claims.getId(),
                accessToken,
                accessExpirationTime,
                TimeUnit.MILLISECONDS
        );

        return accessToken;
    }

    public String generateRefreshToken(Claims claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        redisTemplate.opsForValue().set(
                claims.getId() + refresh,
                refreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    /**
     * 토큰으로부터 클레임을 만들고, 이를 통해 User 객체 생성해 Authentication 객체 반환
     */
    public Authentication getAuthenticationByToken(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if(claims.get("role") == null) throw new AccessDeniedException(ACCESS_DENIED.getMessage());

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("role").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getId(),"", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public String resolveToken(HttpServletRequest request) {
        String accessToken = request.getHeader(accessHeader);
        if (StringUtils.hasText(accessToken)) {
          if (accessToken.startsWith(prefix)) {
              return accessToken.substring(7);
          }
            return accessToken;
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 비밀키를 설정하여 파싱한다.
                    .build()
                    .parseClaimsJws(token);  // 주어진 토큰을 파싱하여 Claims 객체를 얻는다.

            // 토큰의 만료 시간과 현재 시간비교
            return !claims.getBody()
                    .getExpiration()
                    .after(new Date());  // 만료 시간이 현재 시간 이후인지 확인하여 유효성 검사 결과를 반환
        } catch (ExpiredJwtException e) {
            log.error("토큰 이슈 = {}", token);
            log.error("메시지 = {}", e.getMessage());
            log.error("에러 종류 = {}", e.getClass());
            throw new JwtException(EXPIRED_TOKEN.getMessage());
        }
    }

    public HttpHeaders setTokenHeaders(TokenResponse tokenResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(accessHeader, prefix + " " + tokenResponse.getAccessToken());
        headers.add(accessHeader + refresh, prefix + " " + tokenResponse.getRefreshToken());
        return headers;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public void verifyRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(refreshToken);
        } catch (DecodingException e) {
            throw new BaseException(UNSUPPORTED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new BaseException(EXPIRED_TOKEN);
        } catch (SignatureException e) {
            throw new BaseException(WRONG_TYPE_TOKEN);
        }
    }
}
