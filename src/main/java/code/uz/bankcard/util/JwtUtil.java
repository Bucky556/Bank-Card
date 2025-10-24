package code.uz.bankcard.util;

import code.uz.bankcard.dto.JwtDTO;
import code.uz.bankcard.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final long LIVE_TIME_TOKEN = 1000L * 3600 * 24; // 1 day
    private static final String SECRET_KEY = "aEo4QFpxOSQybkMjWGUhbUxyN3RWcDMqRGtZMSZiV3M=";

    public static String encode(String username, UUID id, List<Role> roles) {
        String strRoles = roles.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", strRoles);
        claims.put("id", id.toString());

        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + LIVE_TIME_TOKEN))
                .signWith(getSignInKey())
                .compact();
    }

    private static SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static JwtDTO decode(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        UUID id = UUID.fromString((String) claims.get("id"));
        String rolesStr = (String) claims.get("roles");
        List<Role> roles = Arrays.stream(rolesStr.split(","))
                .map(Role::valueOf)
                .toList();

        return new JwtDTO(username, id, roles);
    }
}
