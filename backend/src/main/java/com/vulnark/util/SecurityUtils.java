package com.vulnark.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 */
public class SecurityUtils {

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("用户未认证，无法获取用户ID");
        }

        // 处理User实体认证（从JwtAuthenticationFilter设置）
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.vulnark.entity.User) {
            com.vulnark.entity.User user = (com.vulnark.entity.User) principal;
            return user.getId();
        }

        // 处理UserDetails认证
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                throw new SecurityException("无法解析用户ID: " + username);
            }
        }

        // 如果是字符串类型的principal
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new SecurityException("无法解析用户ID: " + principal);
            }
        }

        throw new SecurityException("无法获取当前用户ID，未知的认证类型");
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("用户未认证，无法获取用户名");
        }

        // 处理User实体认证（从JwtAuthenticationFilter设置）
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.vulnark.entity.User) {
            com.vulnark.entity.User user = (com.vulnark.entity.User) principal;
            return user.getUsername();
        }

        // 处理UserDetails认证
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        // 如果是字符串类型的principal
        if (principal instanceof String) {
            return (String) principal;
        }

        String name = authentication.getName();
        if (name != null && !"anonymousUser".equals(name)) {
            return name;
        }

        throw new SecurityException("无法获取当前用户名");
    }

    /**
     * 获取当前认证信息
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role) || 
                                     authority.getAuthority().equals(role));
    }

    /**
     * 检查是否有指定权限
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }



    /**
     * 清除安全上下文
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
