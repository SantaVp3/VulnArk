package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.AgentRegistrationRequest;
import com.vulnark.dto.AgentRegistrationResponse;
import com.vulnark.entity.Agent;
import com.vulnark.entity.BaselineTask;
import com.vulnark.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "代理管理", description = "代理节点管理相关接口")
@RestController
@RequestMapping("/api/admin/agents")
@PreAuthorize("hasRole('ADMIN')")
public class AgentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    
    @Autowired
    private AgentService agentService;
    
    @PostMapping("/register")
    @Operation(summary = "Agent注册", description = "新Agent注册到系统")
    public ResponseEntity<ApiResponse<AgentRegistrationResponse>> registerAgent(
            @Valid @RequestBody AgentRegistrationRequest request) {
        try {
            AgentRegistrationResponse response = agentService.registerAgent(request);
            return ResponseEntity.ok(ApiResponse.success("Agent注册成功", response));
        } catch (Exception e) {
            logger.error("Agent注册失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Agent注册失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/heartbeat")
    @Operation(summary = "Agent心跳", description = "Agent定期发送心跳保持在线状态")
    public ResponseEntity<ApiResponse<Void>> heartbeat(
            @Parameter(description = "Agent ID") @RequestHeader("X-Agent-ID") String agentId,
            @Parameter(description = "Agent Token") @RequestHeader("Authorization") String token) {
        try {
            // 验证Token
            if (!agentService.validateAgentToken(agentId, token.replace("Bearer ", ""))) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Token验证失败"));
            }
            
            agentService.heartbeat(agentId);
            return ResponseEntity.ok(ApiResponse.success("心跳成功", null));
        } catch (Exception e) {
            logger.error("心跳处理失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("心跳失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/tasks")
    @Operation(summary = "获取待执行任务", description = "Agent获取分配给它的待执行任务")
    public ResponseEntity<ApiResponse<List<BaselineTask>>> getTasks(
            @Parameter(description = "Agent ID") @RequestHeader("X-Agent-ID") String agentId,
            @Parameter(description = "Agent Token") @RequestHeader("Authorization") String token) {
        try {
            // 验证Token
            if (!agentService.validateAgentToken(agentId, token.replace("Bearer ", ""))) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Token验证失败"));
            }
            
            List<BaselineTask> tasks = agentService.getPendingTasks(agentId);
            return ResponseEntity.ok(ApiResponse.success("获取任务成功", tasks));
        } catch (Exception e) {
            logger.error("获取任务失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取任务失败: " + e.getMessage()));
        }
    }
    
    // 管理端接口 - 需要管理员权限
    
    @GetMapping
    @Operation(summary = "获取Agent列表", description = "分页获取所有Agent信息")
    public ResponseEntity<ApiResponse<Page<Agent>>> getAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Agent.Status status,
            @RequestParam(required = false) Agent.Platform platform) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Agent> agents = agentService.searchAgents(keyword, status, platform, pageable);

            return ResponseEntity.ok(ApiResponse.success("获取Agent列表成功", agents));
        } catch (Exception e) {
            logger.error("获取Agent列表失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取Agent列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{agentId}")
    @Operation(summary = "获取Agent详情", description = "根据Agent ID获取详细信息")
    public ResponseEntity<ApiResponse<Agent>> getAgent(@PathVariable String agentId) {
        try {
            Optional<Agent> agent = agentService.getAgent(agentId);
            if (agent.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("获取Agent详情成功", agent.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("获取Agent详情失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取Agent详情失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{agentId}/status")
    @Operation(summary = "更新Agent状态", description = "手动更新Agent状态")
    public ResponseEntity<ApiResponse<Void>> updateAgentStatus(
            @PathVariable String agentId,
            @RequestParam Agent.Status status) {
        try {
            agentService.updateAgentStatus(agentId, status);
            return ResponseEntity.ok(ApiResponse.success("更新Agent状态成功", null));
        } catch (Exception e) {
            logger.error("更新Agent状态失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新Agent状态失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{agentId}")
    @Operation(summary = "删除Agent", description = "从系统中删除Agent")
    public ResponseEntity<ApiResponse<Void>> deleteAgent(@PathVariable String agentId) {
        try {
            agentService.deleteAgent(agentId);
            return ResponseEntity.ok(ApiResponse.success("删除Agent成功", null));
        } catch (Exception e) {
            logger.error("删除Agent失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除Agent失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    @Operation(summary = "获取Agent统计", description = "获取Agent状态统计信息")
    public ResponseEntity<ApiResponse<AgentService.AgentStats>> getAgentStats() {
        try {
            AgentService.AgentStats stats = agentService.getAgentStats();
            return ResponseEntity.ok(ApiResponse.success("获取Agent统计成功", stats));
        } catch (Exception e) {
            logger.error("获取Agent统计失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取Agent统计失败: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{platform}/{arch}")
    @Operation(summary = "下载Agent客户端", description = "下载指定平台和架构的Agent客户端")
    public ResponseEntity<?> downloadAgent(
            @PathVariable String platform,
            @PathVariable String arch) {
        try {
            // 这里暂时返回一个提示信息，实际实现需要根据具体需求
            String message = String.format("Agent客户端下载功能正在开发中。平台: %s, 架构: %s", platform, arch);
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception e) {
            logger.error("下载Agent客户端失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("下载Agent客户端失败: " + e.getMessage()));
        }
    }
}
