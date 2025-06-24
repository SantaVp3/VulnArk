package com.vulnark.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "统一响应结果")
public class Result<T> {
    
    @Schema(description = "响应码")
    private Integer code;
    
    @Schema(description = "响应消息")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    @Schema(description = "时间戳")
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未授权", null);
    }
    
    public static <T> Result<T> forbidden() {
        return new Result<>(403, "禁止访问", null);
    }
    
    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源不存在", null);
    }
    
    // Getters and Setters
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
