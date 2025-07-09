package com.vulnark.service;

import com.vulnark.entity.ScanTool;
import java.util.List;
import java.util.Map;

/**
 * 工具下载服务接口
 */
public interface ToolDownloadService {

    /**
     * 获取可用的扫描工具列表
     * @return 工具列表
     */
    List<Map<String, Object>> getAvailableTools();

    /**
     * 获取工具下载URL
     * @param toolType 工具类型
     * @param version 版本
     * @param platform 平台
     * @param arch 架构
     * @return 下载URL
     */
    String getDownloadUrl(String toolType, String version, String platform, String arch);

    /**
     * 检查工具安装状态
     * @param toolType 工具类型
     * @return 状态信息
     */
    Map<String, Object> checkToolStatus(String toolType);
    
    /**
     * 下载并安装工具
     * @param tool 要下载安装的工具
     */
    void downloadAndInstallTool(ScanTool tool);
}
