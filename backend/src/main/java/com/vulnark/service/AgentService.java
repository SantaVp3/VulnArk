package com.vulnark.service;

import com.vulnark.dto.AgentRegistrationRequest;
import com.vulnark.dto.AgentRegistrationResponse;
import com.vulnark.entity.Agent;
import com.vulnark.entity.BaselineTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AgentService {
    
    /**
     * 注册新的Agent
     */
    AgentRegistrationResponse registerAgent(AgentRegistrationRequest request);
    
    /**
     * Agent心跳上报
     */
    void heartbeat(String agentId);
    
    /**
     * 获取Agent信息
     */
    Optional<Agent> getAgent(String agentId);
    
    /**
     * 获取所有Agent
     */
    Page<Agent> getAllAgents(Pageable pageable);
    
    /**
     * 根据关键词搜索Agent
     */
    Page<Agent> searchAgents(String keyword, Pageable pageable);

    /**
     * 根据条件搜索Agent
     */
    Page<Agent> searchAgents(String keyword, Agent.Status status, Agent.Platform platform, Pageable pageable);
    
    /**
     * 获取Agent的待执行任务
     */
    List<BaselineTask> getPendingTasks(String agentId);
    
    /**
     * 更新Agent状态
     */
    void updateAgentStatus(String agentId, Agent.Status status);
    
    /**
     * 删除Agent
     */
    void deleteAgent(String agentId);
    
    /**
     * 检查离线Agent
     */
    void checkOfflineAgents();
    
    /**
     * 获取Agent统计信息
     */
    AgentStats getAgentStats();
    
    /**
     * 验证Agent Token
     */
    boolean validateAgentToken(String agentId, String token);
    
    /**
     * Agent统计信息
     */
    class AgentStats {
        private long totalAgents;
        private long onlineAgents;
        private long offlineAgents;
        private long errorAgents;
        private long windowsAgents;
        private long linuxAgents;

        public AgentStats(long totalAgents, long onlineAgents, long offlineAgents, long errorAgents, long windowsAgents, long linuxAgents) {
            this.totalAgents = totalAgents;
            this.onlineAgents = onlineAgents;
            this.offlineAgents = offlineAgents;
            this.errorAgents = errorAgents;
            this.windowsAgents = windowsAgents;
            this.linuxAgents = linuxAgents;
        }

        // Getters
        public long getTotalAgents() { return totalAgents; }
        public long getOnlineAgents() { return onlineAgents; }
        public long getOfflineAgents() { return offlineAgents; }
        public long getErrorAgents() { return errorAgents; }
        public long getWindowsAgents() { return windowsAgents; }
        public long getLinuxAgents() { return linuxAgents; }
    }
}
