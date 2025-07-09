package com.vulnark.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全响应头过滤器
 * 为所有HTTP响应添加安全相关的头信息
 */
@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityHeadersFilter.class);

    @Value("${vulnark.security.headers.enabled:true}")
    private boolean securityHeadersEnabled;

    @Value("${vulnark.security.headers.strict:true}")
    private boolean strictSecurityHeaders;

    @Value("${vulnark.security.csp.enabled:true}")
    private boolean cspEnabled;

    @Value("${vulnark.security.hsts.enabled:true}")
    private boolean hstsEnabled;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (securityHeadersEnabled) {
            addSecurityHeaders(httpRequest, httpResponse);
        }

        chain.doFilter(request, response);
    }

    /**
     * 添加安全响应头
     */
    private void addSecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
        // 1. X-Content-Type-Options: 防止MIME类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");

        // 2. X-Frame-Options: 防止点击劫持
        if (strictSecurityHeaders) {
            response.setHeader("X-Frame-Options", "DENY");
        } else {
            response.setHeader("X-Frame-Options", "SAMEORIGIN");
        }

        // 3. X-XSS-Protection: 启用浏览器XSS保护
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // 4. Referrer-Policy: 控制引用信息
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // 5. X-Permitted-Cross-Domain-Policies: 控制跨域策略文件
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");

        // 6. X-Download-Options: IE下载安全
        response.setHeader("X-Download-Options", "noopen");

        // 7. Cache-Control: 缓存控制
        if (isSecurePath(request.getRequestURI())) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        // 8. Content-Security-Policy: 内容安全策略
        if (cspEnabled) {
            addContentSecurityPolicy(response, request);
        }

        // 9. Strict-Transport-Security: HTTPS强制
        if (hstsEnabled && request.isSecure()) {
            response.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
        }

        // 10. Feature-Policy / Permissions-Policy: 功能策略
        addPermissionsPolicy(response);

        // 11. 移除可能泄露服务器信息的头
        removeServerInfoHeaders(response);

        // 12. 添加自定义安全头
        response.setHeader("X-Vulnark-Security", "enabled");
    }

    /**
     * 添加内容安全策略
     */
    private void addContentSecurityPolicy(HttpServletResponse response, HttpServletRequest request) {
        StringBuilder csp = new StringBuilder();

        if (strictSecurityHeaders) {
            // 严格的CSP策略（生产环境）
            csp.append("default-src 'self'; ");
            csp.append("script-src 'self' 'unsafe-inline' 'unsafe-eval'; ");
            csp.append("style-src 'self' 'unsafe-inline'; ");
            csp.append("img-src 'self' data: blob:; ");
            csp.append("font-src 'self' data:; ");
            csp.append("connect-src 'self'; ");
            csp.append("media-src 'self'; ");
            csp.append("object-src 'none'; ");
            csp.append("child-src 'self'; ");
            csp.append("frame-ancestors 'none'; ");
            csp.append("form-action 'self'; ");
            csp.append("base-uri 'self'; ");
            csp.append("manifest-src 'self'");
        } else {
            // 宽松的CSP策略（开发环境）
            csp.append("default-src 'self' 'unsafe-inline' 'unsafe-eval'; ");
            csp.append("img-src 'self' data: blob: http: https:; ");
            csp.append("font-src 'self' data: http: https:; ");
            csp.append("connect-src 'self' ws: wss: http: https:; ");
            csp.append("frame-ancestors 'self'; ");
            csp.append("object-src 'none'");
        }

        response.setHeader("Content-Security-Policy", csp.toString());
        
        // 同时设置报告模式用于监控
        response.setHeader("Content-Security-Policy-Report-Only", csp.toString());
    }

    /**
     * 添加权限策略
     */
    private void addPermissionsPolicy(HttpServletResponse response) {
        StringBuilder policy = new StringBuilder();
        policy.append("accelerometer=(), ");
        policy.append("ambient-light-sensor=(), ");
        policy.append("autoplay=(), ");
        policy.append("battery=(), ");
        policy.append("camera=(), ");
        policy.append("display-capture=(), ");
        policy.append("document-domain=(), ");
        policy.append("encrypted-media=(), ");
        policy.append("execution-while-not-rendered=(), ");
        policy.append("execution-while-out-of-viewport=(), ");
        policy.append("fullscreen=(), ");
        policy.append("geolocation=(), ");
        policy.append("gyroscope=(), ");
        policy.append("magnetometer=(), ");
        policy.append("microphone=(), ");
        policy.append("midi=(), ");
        policy.append("navigation-override=(), ");
        policy.append("payment=(), ");
        policy.append("picture-in-picture=(), ");
        policy.append("publickey-credentials-get=(), ");
        policy.append("speaker-selection=(), ");
        policy.append("sync-xhr=(), ");
        policy.append("usb=(), ");
        policy.append("wake-lock=(), ");
        policy.append("xr-spatial-tracking=()");

        response.setHeader("Permissions-Policy", policy.toString());
    }

    /**
     * 移除服务器信息头
     */
    private void removeServerInfoHeaders(HttpServletResponse response) {
        // 移除可能泄露服务器信息的头
        String[] headersToRemove = {
            "Server",
            "X-Powered-By",
            "X-AspNet-Version",
            "X-AspNetMvc-Version",
            "X-Frame-Options"  // 我们自己会设置这个头
        };

        for (String header : headersToRemove) {
            if (response.containsHeader(header)) {
                response.setHeader(header, "");
            }
        }
    }

    /**
     * 判断是否是安全路径（需要严格缓存控制）
     */
    private boolean isSecurePath(String path) {
        String[] securePaths = {
            "/api/auth/",
            "/api/user/",
            "/api/admin/",
            "/api/vulnerability/",
            "/api/scan/",
            "/api/agent/"
        };

        for (String securePath : securePaths) {
            if (path.startsWith(securePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("安全响应头过滤器已初始化 - 启用状态: {}, 严格模式: {}", 
            securityHeadersEnabled, strictSecurityHeaders);
    }

    @Override
    public void destroy() {
        logger.info("安全响应头过滤器已销毁");
    }
} 