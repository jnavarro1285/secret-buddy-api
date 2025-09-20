package com.navexa.secretbuddy.core.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class TokenService {
    private final byte[] secret;

    public TokenService(String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String issueJoinToken(UUID eventId, String phoneE164) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] sig = mac.doFinal((eventId + ":" + phoneE164).getBytes(StandardCharsets.UTF_8));
// shorten by taking first 16 bytes and base64url
            byte[] prefix = new byte[16];
            System.arraycopy(sig, 0, prefix, 0, 16);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(prefix);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot issue token", e);
        }
    }
}