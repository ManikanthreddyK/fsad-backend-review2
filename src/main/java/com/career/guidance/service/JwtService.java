package com.career.guidance.service;

import com.career.guidance.model.UserRole;
import com.career.guidance.util.EnvUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final long EXPIRY_SECONDS = 60L * 60L * 24L;

    private final String secret = EnvUtils.get("JWT_SECRET", "change-this-jwt-secret");

    public String generateToken(Long userId, String email, String fullName, UserRole role) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiration = issuedAt + EXPIRY_SECONDS;
        String header = encodeJson("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encodeJson(String.format(
                "{\"sub\":\"%s\",\"email\":\"%s\",\"name\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
                escape(userId.toString()), escape(email), escape(fullName), escape(role.name()), issuedAt, expiration
        ));
        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    public Map<String, String> parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Missing token");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!expectedSignature.equals(parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }
        String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        Map<String, String> claims = readFlatJson(payloadJson);
        long expiration = Long.parseLong(claims.getOrDefault("exp", "0"));
        if (Instant.now().getEpochSecond() > expiration) {
            throw new IllegalArgumentException("Token expired");
        }
        return claims;
    }

    private String encodeJson(String json) {
        return URL_ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign JWT", ex);
        }
    }

    private Map<String, String> readFlatJson(String json) {
        Map<String, String> claims = new HashMap<>();
        String sanitized = json.trim();
        if (sanitized.startsWith("{") && sanitized.endsWith("}")) {
            sanitized = sanitized.substring(1, sanitized.length() - 1);
        }
        if (sanitized.isBlank()) {
            return claims;
        }
        for (String pair : sanitized.split(",")) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            claims.put(key, value);
        }
        return claims;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
