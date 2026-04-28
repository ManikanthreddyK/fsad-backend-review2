package com.career.guidance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class RecaptchaService {
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${RECAPTCHA_SECRET_KEY:}")
    private String recaptchaSecretKey;

    public boolean verifyToken(String token) {
        if (recaptchaSecretKey == null || recaptchaSecretKey.isBlank()) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "CAPTCHA secret key is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", recaptchaSecretKey);
        form.add("response", token);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    VERIFY_URL,
                    entity,
                    Map.class
            );
            return Boolean.TRUE.equals(response == null ? null : response.get("success"));
        } catch (RestClientException ex) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Unable to verify CAPTCHA token");
        }
    }
}
