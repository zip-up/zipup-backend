package com.zipup.server.global.security.util;

import com.zipup.server.global.exception.BaseException;
import com.zipup.server.user.dto.TokenResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
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
    private final JwtProperties jwtProperties;
    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = createSecretKey(jwtProperties.getSecret());
    }

    protected Key createSecretKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
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
        Date expireDate = new Date(now.getTime() + jwtProperties.getAccessExpirationTime());

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        redisTemplate.opsForValue().set(
                claims.getId(),
                accessToken,
                jwtProperties.getAccessExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        return accessToken;
    }

    public String generateRefreshToken(Claims claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getRefreshExpirationTime());

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        redisTemplate.opsForValue().set(
                claims.getId() + jwtProperties.getSuffix(),
                refreshToken,
                jwtProperties.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

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
        String accessToken = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(accessToken)) {
          if (accessToken.startsWith(jwtProperties.getPrefix())) {
              return accessToken.substring(7);
          }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody()
                    .getExpiration()
                    .after(new Date());
        } catch (ExpiredJwtException e) {
            log.error("토큰 이슈 = {}", token);
            log.error("메시지 = {}", e.getMessage());
            log.error("에러 종류 = {}", e.getClass());
            throw new JwtException(EXPIRED_TOKEN.getMessage());
        }
    }

    public HttpHeaders setTokenHeaders(TokenResponse tokenResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(jwtProperties.getHeader(), jwtProperties.getPrefix() + " " + tokenResponse.getAccessToken());
        headers.add(jwtProperties.getHeader() + jwtProperties.getSuffix(), jwtProperties.getPrefix() + " " + tokenResponse.getRefreshToken());
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
        } catch (DecodingException | UnsupportedJwtException e) {
            log.error("jwt provider :: {} {}", UNSUPPORTED_TOKEN.getMessage(), StringUtils.hasText(refreshToken));
            throw new BaseException(UNSUPPORTED_TOKEN);
        } catch (ExpiredJwtException e) {
            log.error("jwt provider :: {} {}", EXPIRED_REFRESH_TOKEN.getMessage(), StringUtils.hasText(refreshToken));
            throw new BaseException(EXPIRED_REFRESH_TOKEN);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("jwt provider :: {} {}", WRONG_TYPE_TOKEN.getMessage(), StringUtils.hasText(refreshToken));
            throw new BaseException(WRONG_TYPE_TOKEN);
        }
    }
}
