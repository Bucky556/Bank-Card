package code.uz.bankcard.util;

import code.uz.bankcard.dto.JwtDTO;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.exception.BadException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final int liveTimeToken = 1000 * 3600 * 24;
    private static final String secretKey = "aEo4QFpxOSQybkMjWGUhbUxyN3RWcDMqRGtZMSZiV3M=";

    public static String encode(String username, UUID id, List<Role> role) {
        String strRoles = role.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        Map<String, String> claims = new HashMap<>();
        claims.put("roles", strRoles);
        claims.put("id", id.toString());

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + liveTimeToken))
                .signWith(getSignInKey())
                .compact();
    }

    private static SecretKey getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    public static JwtDTO decode(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        UUID id = UUID.fromString(claims.get("id", String.class));
        String roles = (String) claims.get("roles");
        List<Role> roleList = Arrays.stream(roles.split(","))
                .map(Role::valueOf)
                .toList();

        return new JwtDTO(username, id, roleList);
    }
}
