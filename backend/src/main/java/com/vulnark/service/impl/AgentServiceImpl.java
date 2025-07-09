package com.vulnark.service.impl;

import com.vulnark.dto.AgentRegistrationRequest;
import com.vulnark.dto.AgentRegistrationResponse;
import com.vulnark.entity.Agent;
import com.vulnark.entity.BaselineTask;
import com.vulnark.repository.AgentRepository;
import com.vulnark.repository.BaselineTaskRepository;
import com.vulnark.security.JwtTokenProvider;
import com.vulnark.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AgentServiceImpl implements AgentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);
    
    private static final int HEARTBEAT_INTERVAL = 30; // 30秒
    private static final int TASK_POLL_INTERVAL = 60; // 60秒
    private static final int OFFLINE_THRESHOLD_MINUTES = 5; // 5分钟无心跳视为离线
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private BaselineTaskRepository baselineTaskRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    public AgentRegistrationResponse registerAgent(AgentRegistrationRequest request) {
        logger.info("注册新Agent: {}", request.getName());
        
        // 检查是否已存在相同的Agent
        if (agentRepository.existsByHostnameAndIpAddress(request.getHostname(), request.getIpAddress())) {
            throw new RuntimeException("该主机已注册Agent");
        }
        
        // 生成唯一的Agent ID
        String agentId = UUID.randomUUID().toString();
        
        // 创建Agent实体
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setName(request.getName());
        agent.setHostname(request.getHostname());
        agent.setIpAddress(request.getIpAddress());
        agent.setPlatform(Agent.Platform.valueOf(request.getPlatform()));
        agent.setOsVersion(request.getOsVersion());
        agent.setAgentVersion(request.getAgentVersion());
        agent.setDescription(request.getDescription());
        agent.setStatus(Agent.Status.ONLINE);
        agent.setLastHeartbeat(LocalDateTime.now());
        
        // 保存到数据库
        agentRepository.save(agent);
        
        // 生成JWT Token
        String token = jwtTokenProvider.generateAgentToken(agentId);
        
        logger.info("Agent注册成功: {} ({})", request.getName(), agentId);
        
        return new AgentRegistrationResponse(
            agentId,
            token,
            System.currentTimeMillis(),
            HEARTBEAT_INTERVAL,
            TASK_POLL_INTERVAL
        );
    }
    
    @Override
    public void heartbeat(String agentId) {
        Optional<Agent> agentOpt = agentRepository.findByAgentId(agentId);
        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            agent.setLastHeartbeat(LocalDateTime.now());
            agent.setStatus(Agent.Status.ONLINE);
            agentRepository.save(agent);
            logger.debug("收到Agent心跳: {}", agentId);
        } else {
            logger.warn("未找到Agent: {}", agentId);
            throw new RuntimeException("Agent不存在");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Agent> getAgent(String agentId) {
        return agentRepository.findByAgentId(agentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Agent> getAllAgents(Pageable pageable) {
        return agentRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Agent> searchAgents(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAgents(pageable);
        }
        return agentRepository.findByKeyword(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Agent> searchAgents(String keyword, Agent.Status status, Agent.Platform platform, Pageable pageable) {
        return agentRepository.findByConditions(keyword, status, platform, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaselineTask> getPendingTasks(String agentId) {
        List<BaselineTask.Status> pendingStatuses = Arrays.asList(
            BaselineTask.Status.PENDING,
            BaselineTask.Status.RUNNING
        );
        return baselineTaskRepository.findPendingTasksByAgentId(agentId, pendingStatuses);
    }
    
    @Override
    public void updateAgentStatus(String agentId, Agent.Status status) {
        Optional<Agent> agentOpt = agentRepository.findByAgentId(agentId);
        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            agent.setStatus(status);
            agentRepository.save(agent);
            logger.info("更新Agent状态: {} -> {}", agentId, status);
        } else {
            throw new RuntimeException("Agent不存在");
        }
    }
    
    @Override
    public void deleteAgent(String agentId) {
        Optional<Agent> agentOpt = agentRepository.findByAgentId(agentId);
        if (agentOpt.isPresent()) {
            agentRepository.delete(agentOpt.get());
            logger.info("删除Agent: {}", agentId);
        } else {
            throw new RuntimeException("Agent不存在");
        }
    }
    
    @Override
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkOfflineAgents() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES);
        List<Agent> offlineAgents = agentRepository.findOfflineAgents(threshold);
        
        for (Agent agent : offlineAgents) {
            if (agent.getStatus() == Agent.Status.ONLINE) {
                agent.setStatus(Agent.Status.OFFLINE);
                agentRepository.save(agent);
                logger.warn("Agent离线: {} ({})", agent.getName(), agent.getAgentId());
            }
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public AgentStats getAgentStats() {
        long totalAgents = agentRepository.count();
        long onlineAgents = agentRepository.countByStatus(Agent.Status.ONLINE);
        long offlineAgents = agentRepository.countByStatus(Agent.Status.OFFLINE);
        long errorAgents = agentRepository.countByStatus(Agent.Status.ERROR);
        long windowsAgents = agentRepository.countByPlatform(Agent.Platform.WINDOWS);
        long linuxAgents = agentRepository.countByPlatform(Agent.Platform.LINUX);

        return new AgentStats(totalAgents, onlineAgents, offlineAgents, errorAgents, windowsAgents, linuxAgents);
    }
    
    @Override
    public boolean validateAgentToken(String agentId, String token) {
        try {
            String tokenAgentId = jwtTokenProvider.getAgentIdFromToken(token);
            return agentId.equals(tokenAgentId) && jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            logger.warn("Agent Token验证失败: {}", agentId);
            return false;
        }
    }
}
