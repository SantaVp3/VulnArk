package com.vulnark.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 密码安全工具类
 * 用于生成强密码、验证密码强度和处理密码哈希
 */
public class PasswordTestUtil {
    
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    // 密码强度正则表达式
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$"
    );
    
    /**
     * 生成强密码
     * 
     * @param length 密码长度（最少12位）
     * @return 强密码
     */
    public static String generateStrongPassword(int length) {
        if (length < 12) {
            throw new IllegalArgumentException("密码长度至少为12位");
        }
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // 确保包含各种字符类型
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        
        // 填充剩余位数
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // 打乱密码字符顺序
        return shuffleString(password.toString(), random);
    }
    
    /**
     * 验证密码强度
     * 
     * @param password 待验证密码
     * @return 是否为强密码
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 12) {
            return false;
        }
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * 获取密码强度描述
     * 
     * @param password 待检查密码
     * @return 强度描述
     */
    public static String getPasswordStrengthDescription(String password) {
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }
        
        if (password.length() < 8) {
            return "弱密码：长度不足8位";
        }
        
        if (password.length() < 12) {
            return "中等密码：建议至少12位";
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> SPECIAL_CHARS.indexOf(ch) >= 0);
        
        int strengthScore = 0;
        if (hasUpper) strengthScore++;
        if (hasLower) strengthScore++;
        if (hasDigit) strengthScore++;
        if (hasSpecial) strengthScore++;
        
        if (strengthScore == 4 && password.length() >= 12) {
            return "强密码";
        } else if (strengthScore >= 3) {
            return "中等密码：" + getMissingRequirements(hasUpper, hasLower, hasDigit, hasSpecial);
        } else {
            return "弱密码：" + getMissingRequirements(hasUpper, hasLower, hasDigit, hasSpecial);
        }
    }
    
    private static String getMissingRequirements(boolean hasUpper, boolean hasLower, boolean hasDigit, boolean hasSpecial) {
        StringBuilder missing = new StringBuilder("缺少：");
        if (!hasUpper) missing.append("大写字母 ");
        if (!hasLower) missing.append("小写字母 ");
        if (!hasDigit) missing.append("数字 ");
        if (!hasSpecial) missing.append("特殊字符 ");
        return missing.toString().trim();
    }
    
    private static String shuffleString(String string, SecureRandom random) {
        char[] chars = string.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        System.out.println("=== 强密码生成和验证工具 ===");
        
        // 生成强密码示例
        System.out.println("\n--- 强密码生成 ---");
        for (int i = 0; i < 3; i++) {
            String strongPassword = generateStrongPassword(16);
            String hash = passwordEncoder.encode(strongPassword);
            
            System.out.println("生成的强密码: " + strongPassword);
            System.out.println("密码强度: " + getPasswordStrengthDescription(strongPassword));
            System.out.println("BCrypt哈希: " + hash);
            System.out.println("验证结果: " + passwordEncoder.matches(strongPassword, hash));
            System.out.println("---");
        }
        
        // 密码强度测试
        System.out.println("\n--- 密码强度测试 ---");
        String[] testPasswords = {
            "123456",                    // 弱密码
            "password",                  // 弱密码
            "Password123",               // 中等密码
            "MyStr0ng@Password2024!"     // 强密码
        };
        
        for (String testPassword : testPasswords) {
            System.out.println("密码: " + testPassword);
            System.out.println("强度评估: " + getPasswordStrengthDescription(testPassword));
            System.out.println("是否强密码: " + isStrongPassword(testPassword));
            System.out.println("---");
        }
        
        System.out.println("\n=== 安全建议 ===");
        System.out.println("1. 使用至少12位长度的密码");
        System.out.println("2. 包含大写字母、小写字母、数字和特殊字符");
        System.out.println("3. 避免使用常见单词、个人信息或重复字符");
        System.out.println("4. 定期更换密码");
        System.out.println("5. 不要在多个系统中重复使用相同密码");
    }
}
