package com.vulnark.exception;

import com.vulnark.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局异常处理器
 * 统一处理应用中的所有异常，防止敏感信息泄露
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:development}")
    private String activeProfile;

    @Value("${vulnark.security.error.include-stacktrace:false}")
    private boolean includeStackTrace;

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "业务异常", ex, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            Exception ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "认证异常", ex, request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.unauthorized());
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class, java.nio.file.AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            Exception ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "权限异常", ex, request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.forbidden());
    }

    /**
     * 处理数据验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            Exception ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "数据验证异常", ex, request);

        Map<String, String> errors = new HashMap<>();
        
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException methodEx = (MethodArgumentNotValidException) ex;
            methodEx.getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (ex instanceof BindException) {
            BindException bindEx = (BindException) ex;
            bindEx.getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        }

        String message = errors.isEmpty() ? "数据验证失败" : "数据验证失败：" + errors.toString();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "约束违反异常", ex, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("数据验证失败"));
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "参数类型异常", ex, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("参数格式错误"));
    }

    /**
     * 处理数据库完整性约束异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "数据完整性异常", ex, request);

        // 根据异常信息判断具体的约束违反类型
        String message = "数据操作失败";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                message = "数据已存在，请检查唯一性约束";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "相关数据不存在，请检查关联关系";
            } else if (ex.getMessage().contains("cannot be null")) {
                message = "必填字段不能为空";
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Object>> handleSQLException(
            SQLException ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "数据库异常", ex, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("数据库操作失败，错误编号：" + errorId));
    }

    /**
     * 处理安全异常
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Object>> handleSecurityException(
            SecurityException ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "安全异常", ex, request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("安全验证失败"));
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "运行时异常", ex, request);

        // 在开发环境中可以显示更多错误信息
        String message = "development".equals(activeProfile) 
            ? "运行时错误：" + ex.getMessage() 
            : "系统内部错误，错误编号：" + errorId;

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        
        String errorId = generateErrorId();
        logError(errorId, "未处理异常", ex, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统内部错误，错误编号：" + errorId));
    }

    /**
     * 生成错误ID
     */
    private String generateErrorId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 记录错误日志
     */
    private void logError(String errorId, String errorType, Exception ex, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        logger.error("错误ID: {} | 类型: {} | IP: {} | URI: {} {} | User-Agent: {} | 错误: {}", 
            errorId, errorType, clientIp, method, requestUri, userAgent, ex.getMessage());
        
        // 在开发环境中记录完整堆栈信息
        if (includeStackTrace || "development".equals(activeProfile)) {
            logger.debug("错误ID: {} 的完整堆栈信息:", errorId, ex);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 