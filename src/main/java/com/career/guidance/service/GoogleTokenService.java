package com.career.guidance.service;

import com.career.guidance.util.EnvUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenService {
    private final String googleClientId = EnvUtils.get("GOOGLE_CLIENT_ID", "");

    public GoogleProfile verify(String credential) {
        if (googleClientId.isBlank()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID is not configured");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google credential");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            return new GoogleProfile(
                    payload.getEmail(),
                    (String) payload.get("name")
            );
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("Could not verify Google token", ex);
        }
    }

    public record GoogleProfile(String email, String name) {
    }
}
