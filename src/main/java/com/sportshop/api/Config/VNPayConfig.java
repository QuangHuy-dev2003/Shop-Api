package com.sportshop.api.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Configuration
public class VNPayConfig {

    @Value("${vnp.TmnCode}")
    private String vnp_TmnCode;

    @Value("${vnp.HashSecret}")
    private String vnp_HashSecret;

    @Value("${vnp.Url}")
    private String vnp_PayUrl;

    @Value("${vnp.ReturnUrl}")
    private String vnp_ReturnUrl;

    public String getTmnCode() {
        return vnp_TmnCode;
    }

    public String getHashSecret() {
        return vnp_HashSecret;
    }

    public String getPayUrl() {
        return vnp_PayUrl;
    }

    public String getReturnUrl() {
        return vnp_ReturnUrl;
    }

    public String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secret_key);
            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
