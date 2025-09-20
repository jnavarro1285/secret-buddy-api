package com.navexa.secretbuddy.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class PhoneUtil {

    public static String normalizeE164(String phone) {
// Minimal normalizer: strip non-digits except leading '+'.
        String p = phone.trim().replaceAll("[^+0-9]", "");
        if (!p.startsWith("+")) {
// you may inject default country code here if desired
            p = "+" + p;
        }
        return p;
    }


    public static String sha256Base64(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash", e);
        }
    }
}