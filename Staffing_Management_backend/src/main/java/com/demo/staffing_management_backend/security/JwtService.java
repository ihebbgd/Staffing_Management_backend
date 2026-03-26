package com.demo.staffing_management_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_VERSION = "ver";
    private static final String CLAIM_ROLE = "role";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration:900000}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    /** The signing key is derived once, at startup, from the Base64-decoded secret and reused for every token. */
    private SecretKey signInKey;

    @PostConstruct
    void validateSecret() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("jwt.secret must be configured (set the JWT_SECRET environment variable)");
        }
        final byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (DecodingException ex) {
            throw new IllegalStateException("jwt.secret must be a Base64-encoded value "
                    + "(generate one with: openssl rand -base64 48)", ex);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must decode to at least 32 bytes (256 bits) for HS256");
        }
        this.signInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TYPE, String.class));
    }

    public Integer extractTokenVersion(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_VERSION, Integer.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails, int tokenVersion, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, TYPE_ACCESS);
        claims.put(CLAIM_VERSION, tokenVersion);
        claims.put(CLAIM_ROLE, role);
        return buildToken(claims, userDetails, accessExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails, int tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, TYPE_REFRESH);
        claims.put(CLAIM_VERSION, tokenVersion);
        return buildToken(claims, userDetails, refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expirationMs) {
        final Date now = new Date(System.currentTimeMillis());
        final Date expiry = new Date(System.currentTimeMillis() + expirationMs);
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return signInKey;
    }
}
