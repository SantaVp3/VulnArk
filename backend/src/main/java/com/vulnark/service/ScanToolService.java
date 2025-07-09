package com.vulnark.service;

import com.vulnark.entity.ScanTool;
import com.vulnark.repository.ScanToolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 扫描工具管理服务
 */
@Service
@Transactional
public class ScanToolService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanToolService.class);
    
    @Autowired
    private ScanToolRepository scanToolRepository;
    
    @Autowired
    private ToolDownloadService toolDownloadService;
    
    @Value("${vulnark.tools.base-path:/opt/vulnark/tools}")
    private String toolsBasePath;
    
    /**
     * 初始化默认工具
     */
    public void initializeDefaultTools() {
        logger.info("初始化默认扫描工具...");
        
        // 初始化 Xray
        initializeXray();
        
        // 初始化 Nuclei
        initializeNuclei();
        
        logger.info("默认扫描工具初始化完成");
    }
    
    /**
     * 初始化 Xray
     */
    private void initializeXray() {
        Optional<ScanTool> xrayOpt = scanToolRepository.findByName("xray");
        if (xrayOpt.isEmpty()) {
            // 检测操作系统和架构
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();

            String platform;
            String executable;
            if (os.contains("mac") || os.contains("darwin")) {
                platform = arch.contains("aarch64") || arch.contains("arm") ? "darwin_arm64" : "darwin_amd64";
                executable = "xray_" + platform;
            } else if (os.contains("win")) {
                platform = arch.contains("64") ? "windows_amd64.exe" : "windows_386.exe";
                executable = "xray_" + platform;
            } else {
                platform = arch.contains("64") ? "linux_amd64" : "linux_386";
                executable = "xray_" + platform;
            }

            ScanTool xray = new ScanTool("xray", "Xray 安全扫描器");
            xray.setInstallPath(toolsBasePath + "/xray/" + executable);
            xray.setConfigPath(toolsBasePath + "/xray/config.yaml");
            xray.setLatestVersion("1.9.11");
            xray.setDownloadUrl("https://github.com/chaitin/xray/releases/download/1.9.11/xray_" + platform + ".zip");
            scanToolRepository.save(xray);
            logger.info("已创建 Xray 工具配置，平台: {}, 可执行文件: {}", platform, executable);
        }
    }
    
    /**
     * 初始化 Nuclei
     */
    private void initializeNuclei() {
        Optional<ScanTool> nucleiOpt = scanToolRepository.findByName("nuclei");
        if (nucleiOpt.isEmpty()) {
            // 检测操作系统和架构
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();

            String platform;
            String executable;
            if (os.contains("mac") || os.contains("darwin")) {
                platform = arch.contains("aarch64") || arch.contains("arm") ? "darwin_arm64" : "darwin_amd64";
                executable = "nuclei";
            } else if (os.contains("win")) {
                platform = arch.contains("64") ? "windows_amd64" : "windows_386";
                executable = "nuclei.exe";
            } else {
                platform = arch.contains("64") ? "linux_amd64" : "linux_386";
                executable = "nuclei";
            }

            ScanTool nuclei = new ScanTool("nuclei", "Nuclei 漏洞扫描器");
            nuclei.setInstallPath(toolsBasePath + "/nuclei/" + executable);
            nuclei.setConfigPath(toolsBasePath + "/nuclei/config.yaml");
            nuclei.setLatestVersion("3.1.0");
            nuclei.setDownloadUrl("https://github.com/projectdiscovery/nuclei/releases/download/v3.1.0/nuclei_3.1.0_" + platform + ".zip");
            scanToolRepository.save(nuclei);
            logger.info("已创建 Nuclei 工具配置，平台: {}, 可执行文件: {}", platform, executable);
        }
    }
    
    /**
     * 获取所有工具
     */
    public List<ScanTool> getAllTools() {
        return scanToolRepository.findAll();
    }
    
    /**
     * 根据名称获取工具
     */
    public Optional<ScanTool> getToolByName(String name) {
        return scanToolRepository.findByName(name);
    }
    
    /**
     * 获取已安装的工具
     */
    public List<ScanTool> getInstalledTools() {
        return scanToolRepository.findAllInstalled();
    }
    
    /**
     * 检查工具更新
     */
    public void checkUpdates() {
        logger.info("检查工具更新...");
        List<ScanTool> tools = scanToolRepository.findAll();
        
        for (ScanTool tool : tools) {
            try {
                checkToolUpdate(tool);
            } catch (Exception e) {
                logger.error("检查工具 {} 更新失败: {}", tool.getName(), e.getMessage());
            }
        }
    }
    
    /**
     * 检查单个工具更新
     */
    private void checkToolUpdate(ScanTool tool) {
        tool.setLastCheckTime(LocalDateTime.now());
        
        // TODO: 实现从GitHub API获取最新版本
        // 这里暂时使用模拟数据
        String latestVersion = getLatestVersionFromGitHub(tool.getName());
        if (latestVersion != null) {
            tool.setLatestVersion(latestVersion);
            
            if (tool.getCurrentVersion() != null && !tool.getCurrentVersion().equals(latestVersion)) {
                tool.setStatus(ScanTool.ToolStatus.OUTDATED);
            }
        }
        
        scanToolRepository.save(tool);
    }
    
    /**
     * 从GitHub获取最新版本（模拟实现）
     */
    private String getLatestVersionFromGitHub(String toolName) {
        // TODO: 实现真实的GitHub API调用
        switch (toolName) {
            case "xray":
                return "1.9.11";
            case "nuclei":
                return "3.1.0";
            default:
                return null;
        }
    }
    
    /**
     * 安装或更新工具
     */
    public void installOrUpdateTool(String toolName) {
        Optional<ScanTool> toolOpt = scanToolRepository.findByName(toolName);
        if (toolOpt.isEmpty()) {
            throw new RuntimeException("工具不存在: " + toolName);
        }

        ScanTool tool = toolOpt.get();

        if (tool.getStatus() == ScanTool.ToolStatus.INSTALLING ||
            tool.getStatus() == ScanTool.ToolStatus.UPDATING) {
            throw new RuntimeException("工具正在安装或更新中");
        }

        // 检查是否为开发环境
        if (isDevelopmentEnvironment()) {
            // 开发环境：模拟安装
            simulateToolInstallation(tool);
        } else {
            // 生产环境：真实下载安装
            // 设置状态
            if (tool.isInstalled()) {
                tool.setStatus(ScanTool.ToolStatus.UPDATING);
            } else {
                tool.setStatus(ScanTool.ToolStatus.INSTALLING);
            }
            tool.setDownloadProgress(0);
            tool.setErrorMessage(null);
            scanToolRepository.save(tool);

            // 异步下载和安装
            toolDownloadService.downloadAndInstallTool(tool);
        }
    }

    /**
     * 检查是否为开发环境
     */
    private boolean isDevelopmentEnvironment() {
        // 检查是否在IDE中运行或者是开发模式
        String classPath = System.getProperty("java.class.path");
        return classPath.contains("target/classes") || classPath.contains("build/classes");
    }

    /**
     * 模拟工具安装（开发环境）
     */
    private void simulateToolInstallation(ScanTool tool) {
        logger.info("开发环境：模拟安装工具 {}", tool.getName());

        // 设置状态
        if (tool.isInstalled()) {
            tool.setStatus(ScanTool.ToolStatus.UPDATING);
        } else {
            tool.setStatus(ScanTool.ToolStatus.INSTALLING);
        }
        tool.setDownloadProgress(0);
        scanToolRepository.save(tool);

        // 模拟安装过程
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i += 20) {
                    Thread.sleep(500); // 模拟下载时间
                    tool.setDownloadProgress(i);
                    scanToolRepository.save(tool);
                }

                // 标记为已安装
                tool.setStatus(ScanTool.ToolStatus.INSTALLED);
                tool.setCurrentVersion(tool.getLatestVersion());
                tool.setDownloadProgress(100);
                tool.setLastUpdateTime(LocalDateTime.now());
                tool.setErrorMessage(null);
                scanToolRepository.save(tool);

                logger.info("开发环境：工具 {} 模拟安装完成", tool.getName());

            } catch (InterruptedException e) {
                tool.setStatus(ScanTool.ToolStatus.ERROR);
                tool.setErrorMessage("模拟安装被中断");
                scanToolRepository.save(tool);
            }
        }).start();
    }
    
    /**
     * 获取工具状态
     */
    public Map<String, Object> getToolStatus(String toolName) {
        Optional<ScanTool> toolOpt = scanToolRepository.findByName(toolName);
        if (toolOpt.isEmpty()) {
            throw new RuntimeException("工具不存在: " + toolName);
        }
        
        ScanTool tool = toolOpt.get();
        Map<String, Object> status = new HashMap<>();
        status.put("name", tool.getName());
        status.put("displayName", tool.getDisplayName());
        status.put("status", tool.getStatus().toString());
        status.put("currentVersion", tool.getCurrentVersion());
        status.put("latestVersion", tool.getLatestVersion());
        status.put("downloadProgress", tool.getDownloadProgress());
        status.put("errorMessage", tool.getErrorMessage());
        status.put("needsUpdate", tool.needsUpdate());
        status.put("isInstalled", tool.isInstalled());
        status.put("lastCheckTime", tool.getLastCheckTime());
        status.put("lastUpdateTime", tool.getLastUpdateTime());
        
        return status;
    }
    
    /**
     * 获取工具统计信息
     */
    public Map<String, Object> getToolStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        List<ScanTool> allTools = scanToolRepository.findAll();
        statistics.put("totalTools", allTools.size());
        
        List<ScanTool> installedTools = scanToolRepository.findAllInstalled();
        statistics.put("installedTools", installedTools.size());
        
        List<ScanTool> needUpdateTools = scanToolRepository.findNeedUpdate();
        statistics.put("needUpdateTools", needUpdateTools.size());
        
        // 状态分布
        Map<String, Long> statusDistribution = new HashMap<>();
        List<Object[]> statusCounts = scanToolRepository.countByStatus();
        for (Object[] row : statusCounts) {
            statusDistribution.put(row[0].toString(), (Long) row[1]);
        }
        statistics.put("statusDistribution", statusDistribution);
        
        return statistics;
    }
    
    /**
     * 验证工具是否可用
     */
    public boolean isToolAvailable(String toolName) {
        Optional<ScanTool> toolOpt = scanToolRepository.findByName(toolName);
        if (toolOpt.isEmpty()) {
            return false;
        }
        
        ScanTool tool = toolOpt.get();
        if (!tool.isInstalled()) {
            return false;
        }
        
        // 检查文件是否存在
        File toolFile = new File(tool.getInstallPath());
        return toolFile.exists() && toolFile.canExecute();
    }
    

}
