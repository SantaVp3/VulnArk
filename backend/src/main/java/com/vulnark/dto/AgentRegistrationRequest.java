package com.vulnark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Agent注册请求")
public class AgentRegistrationRequest {
    
    @Schema(description = "Agent名称", required = true)
    @NotBlank(message = "Agent名称不能为空")
    private String name;
    
    @Schema(description = "主机名", required = true)
    @NotBlank(message = "主机名不能为空")
    private String hostname;
    
    @Schema(description = "IP地址", required = true)
    @NotBlank(message = "IP地址不能为空")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "IP地址格式不正确")
    private String ipAddress;
    
    @Schema(description = "操作系统平台", required = true, allowableValues = {"WINDOWS", "LINUX"})
    @NotBlank(message = "操作系统平台不能为空")
    @Pattern(regexp = "^(WINDOWS|LINUX)$", message = "平台必须是WINDOWS或LINUX")
    private String platform;
    
    @Schema(description = "操作系统版本", required = true)
    @NotBlank(message = "操作系统版本不能为空")
    private String osVersion;
    
    @Schema(description = "Agent版本", required = true)
    @NotBlank(message = "Agent版本不能为空")
    private String agentVersion;
    
    @Schema(description = "描述信息")
    private String description;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
    
    public String getAgentVersion() {
        return agentVersion;
    }
    
    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
