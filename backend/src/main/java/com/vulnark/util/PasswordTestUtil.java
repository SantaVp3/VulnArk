package com.vulnark.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码测试工具类
 * 用于生成和验证密码哈希
 */
public class PasswordTestUtil {
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String[] passwords = {"password123", "admin123", "123456"};
        
        System.out.println("=== 密码哈希生成 ===");
        for (String password : passwords) {
            String hash = passwordEncoder.encode(password);
            System.out.println("密码: " + password);
            System.out.println("哈希: " + hash);
            System.out.println("验证: " + passwordEncoder.matches(password, hash));
            System.out.println("---");
        }
        
        // 测试现有哈希值
        System.out.println("\n=== 现有哈希值验证 ===");
        String[] existingHashes = {
            "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkNvAWGG",
            "$2a$10$9g7zX8q5I9n8F2u3t4D5A.B6C7H8E9F0G1H2I3J4K5L6M7N8O9P0Q1"
        };
        
        for (String hash : existingHashes) {
            System.out.println("哈希: " + hash);
            for (String password : passwords) {
                boolean matches = passwordEncoder.matches(password, hash);
                System.out.println("  密码 '" + password + "' 匹配: " + matches);
            }
            System.out.println("---");
        }
    }
}
