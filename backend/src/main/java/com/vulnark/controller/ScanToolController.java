package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.entity.ScanTool;
import com.vulnark.service.ScanToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描工具管理控制器
 */
@Tag(name = "扫描工具管理", description = "扫描工具管理相关接口")
@RestController
@RequestMapping("/api/scan-tools")
public class ScanToolController {
    
    @Autowired
    private ScanToolService scanToolService;
    
    /**
     * 获取所有扫描工具
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTools() {
        try {
            List<ScanTool> tools = scanToolService.getAllTools();
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", tools
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "查询失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取工具状态
     */
    @GetMapping("/{toolName}/status")
    public ResponseEntity<Map<String, Object>> getToolStatus(@PathVariable String toolName) {
        try {
            Map<String, Object> status = scanToolService.getToolStatus(toolName);
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", status
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "查询工具状态失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 安装或更新工具
     */
    @PostMapping("/{toolName}/install")
    public ResponseEntity<Map<String, Object>> installTool(@PathVariable String toolName) {
        try {
            scanToolService.installOrUpdateTool(toolName);
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "工具安装已开始"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "安装工具失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新工具
     */
    @PostMapping("/{toolName}/update")
    public ResponseEntity<Map<String, Object>> updateTool(@PathVariable String toolName) {
        try {
            scanToolService.installOrUpdateTool(toolName);
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "工具更新已开始"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "更新工具失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 检查所有工具更新
     */
    @PostMapping("/check-updates")
    public ResponseEntity<Map<String, Object>> checkUpdates() {
        try {
            scanToolService.checkUpdates();
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "检查更新完成"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "检查更新失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取工具统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = scanToolService.getToolStatistics();
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", statistics
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "查询统计信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取已安装的工具
     */
    @GetMapping("/installed")
    public ResponseEntity<Map<String, Object>> getInstalledTools() {
        try {
            List<ScanTool> tools = scanToolService.getInstalledTools();
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", tools
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "查询已安装工具失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 检查工具是否可用
     */
    @GetMapping("/{toolName}/available")
    public ResponseEntity<Map<String, Object>> checkToolAvailable(@PathVariable String toolName) {
        try {
            boolean available = scanToolService.isToolAvailable(toolName);
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", Map.of("available", available)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "检查工具可用性失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 初始化默认工具
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeTools() {
        try {
            scanToolService.initializeDefaultTools();
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "工具初始化完成"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "工具初始化失败: " + e.getMessage()
            ));
        }
    }
}
