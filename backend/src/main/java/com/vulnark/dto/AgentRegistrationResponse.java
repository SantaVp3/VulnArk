package com.vulnark.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Agent注册响应")
public class AgentRegistrationResponse {
    
    @Schema(description = "Agent ID")
    private String agentId;
    
    @Schema(description = "认证Token")
    private String token;
    
    @Schema(description = "服务器时间戳")
    private Long serverTime;
    
    @Schema(description = "心跳间隔（秒）")
    private Integer heartbeatInterval;
    
    @Schema(description = "任务轮询间隔（秒）")
    private Integer taskPollInterval;
    
    public AgentRegistrationResponse() {
    }
    
    public AgentRegistrationResponse(String agentId, String token, Long serverTime, 
                                   Integer heartbeatInterval, Integer taskPollInterval) {
        this.agentId = agentId;
        this.token = token;
        this.serverTime = serverTime;
        this.heartbeatInterval = heartbeatInterval;
        this.taskPollInterval = taskPollInterval;
    }
    
    // Getters and Setters
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getServerTime() {
        return serverTime;
    }
    
    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }
    
    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
    
    public Integer getTaskPollInterval() {
        return taskPollInterval;
    }
    
    public void setTaskPollInterval(Integer taskPollInterval) {
        this.taskPollInterval = taskPollInterval;
    }
}
