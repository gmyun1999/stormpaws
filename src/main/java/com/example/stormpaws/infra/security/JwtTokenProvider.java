package com.example.stormpaws.infra.security;

import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.service.ITokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements ITokenProvider {

  // Access Token은 30분, Refresh Token은 7일 유효
  private final long accessTokenValidityInMilliseconds = 1000L * 60 * 30; // 30분
  private final long refreshTokenValidityInMilliseconds = 1000L * 60 * 60 * 24 * 7; // 7일

  private final Key key;

  public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  @Override
  public String createAccessToken(UserModel user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenValidityInMilliseconds);

    return Jwts.builder()
        .setSubject(user.getId())
        .claim("oauthType", user.getOauthType())
        .claim("oauthId", user.getOauthId())
        .claim("email", user.getEmail())
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  @Override
  public String createRefreshToken(UserModel user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

    return Jwts.builder()
        .setSubject(user.getId())
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  @Override
  public Claims parseToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Claims claims = parseToken(token);
      return !claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String getUserIdFromToken(String token) {
    return parseToken(token).getSubject();
  }
}
