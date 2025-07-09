package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.service.ToolDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 扫描工具下载控制器
 */
@RestController
@RequestMapping("/api/admin/tools")
public class ScanToolDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(ScanToolDownloadController.class);

    @Autowired
    private ToolDownloadService toolDownloadService;

    /**
     * 获取可下载的工具列表
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAvailableTools() {
        try {
            var tools = toolDownloadService.getAvailableTools();
            return ResponseEntity.ok(ApiResponse.success("获取可用工具列表成功", tools));
        } catch (Exception e) {
            logger.error("获取可用工具列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取可用工具列表失败: " + e.getMessage()));
        }
    }

    /**
     * 下载指定工具
     */
    @PostMapping("/download/{toolType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> downloadTool(
            @PathVariable String toolType,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String arch) {
        try {
            String downloadUrl = toolDownloadService.getDownloadUrl(toolType, version, platform, arch);
            return ResponseEntity.ok(ApiResponse.success("获取下载链接成功", downloadUrl));
        } catch (Exception e) {
            logger.error("获取工具下载链接失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取工具下载链接失败: " + e.getMessage()));
        }
    }

    /**
     * 检查工具安装状态
     */
    @GetMapping("/status/{toolType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> checkToolStatus(@PathVariable String toolType) {
        try {
            var status = toolDownloadService.checkToolStatus(toolType);
            return ResponseEntity.ok(ApiResponse.success("检查工具状态成功", status));
        } catch (Exception e) {
            logger.error("检查工具状态失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("检查工具状态失败: " + e.getMessage()));
        }
    }
} 