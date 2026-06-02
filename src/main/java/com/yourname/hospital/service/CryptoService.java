package com.yourname.hospital.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;
    private static final String PLAINTEXT_PREFIX = "PLAINTEXT:";

    @Value("${app.crypto.enabled:true}")
    private boolean cryptoEnabled;

    public byte[] generateAesKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return key;
    }

    public String encryptText(String plaintext, byte[] key) {
        if (!cryptoEnabled) {
            byte[] plain = plaintext != null ? plaintext.getBytes(StandardCharsets.UTF_8) : new byte[0];
            return PLAINTEXT_PREFIX + Base64.getEncoder().encodeToString(plain);
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            SecretKey secretKey = new SecretKeySpec(key, AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception ex) {
            throw new IllegalStateException("Encrypt failed", ex);
        }
    }

    public String decryptText(String encrypted, byte[] key) {
        if (encrypted == null) {
            return null;
        }
        if (encrypted.startsWith(PLAINTEXT_PREFIX)) {
            String payload = encrypted.substring(PLAINTEXT_PREFIX.length());
            byte[] plain = Base64.getDecoder().decode(payload);
            return new String(plain, StandardCharsets.UTF_8);
        }
        if (!cryptoEnabled) {
            return encrypted;
        }
        try {
            String[] parts = encrypted.split(":");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            SecretKey secretKey = new SecretKeySpec(key, AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Decrypt failed", ex);
        }
    }

    public String hmacSha256(String data, byte[] key) {
        if (!cryptoEnabled) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC failed", ex);
        }
    }

    public boolean verifyHmacSha256(String data, String expectedBase64, byte[] key) {
        if (!cryptoEnabled) {
            return true;
        }
        if (data == null || expectedBase64 == null || expectedBase64.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] actual = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] expected = Base64.getDecoder().decode(expectedBase64);
            return MessageDigest.isEqual(actual, expected);
        } catch (IllegalArgumentException ex) {
            return false;
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC verify failed", ex);
        }
    }

    public byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 failed", ex);
        }
    }
}
