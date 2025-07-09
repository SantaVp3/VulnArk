package com.vulnark.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration  // 启用JWT安全验证
public class JwtConfig {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    private static final int MIN_SECRET_LENGTH = 32; // 256 bits
    
    @PostConstruct
    public void validateJwtConfig() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException(
                "JWT密钥未配置！请设置环境变量 JWT_SECRET。\n" +
                "生成安全密钥的命令：openssl rand -base64 48"
            );
        }
        
        // 验证密钥长度
        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            // 如果不是Base64编码，直接使用字符串的字节
            decodedKey = jwtSecret.getBytes();
        }
        
        if (decodedKey.length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format("JWT密钥长度不足！当前长度：%d字节，最小要求：%d字节", 
                    decodedKey.length, MIN_SECRET_LENGTH)
            );
        }
        
        // 检查是否使用了默认的不安全密钥
        if (isWeakSecret(jwtSecret)) {
            throw new IllegalStateException(
                "检测到不安全的JWT密钥！请使用强随机密钥。\n" +
                "生成安全密钥的命令：openssl rand -base64 48"
            );
        }
    }
    
    /**
     * 检查是否为弱密钥
     */
    private boolean isWeakSecret(String secret) {
        String[] weakSecrets = {
            "secret",
            "mySecretKey",
            "123456",
            "password",
            "vulnark",
            "default"
        };
        
        String lowerSecret = secret.toLowerCase();
        for (String weak : weakSecrets) {
            if (lowerSecret.contains(weak)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成安全的随机密钥（用于开发环境）
     */
    public static String generateSecureSecret() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA512");
            keyGen.init(512, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法生成安全密钥", e);
        }
    }
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
