package com.vulnark.service.scan;

/**
 * 扫描引擎异常类
 */
public class ScanEngineException extends Exception {
    
    private String engineType;
    private String errorCode;
    
    public ScanEngineException(String message) {
        super(message);
    }
    
    public ScanEngineException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ScanEngineException(String engineType, String errorCode, String message) {
        super(message);
        this.engineType = engineType;
        this.errorCode = errorCode;
    }
    
    public ScanEngineException(String engineType, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.engineType = engineType;
        this.errorCode = errorCode;
    }
    
    public String getEngineType() {
        return engineType;
    }
    
    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ScanEngineException");
        if (engineType != null) {
            sb.append(" [").append(engineType).append("]");
        }
        if (errorCode != null) {
            sb.append(" (").append(errorCode).append(")");
        }
        sb.append(": ").append(getMessage());
        return sb.toString();
    }
}
