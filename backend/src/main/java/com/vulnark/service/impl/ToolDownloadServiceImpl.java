package com.vulnark.service.impl;

import com.vulnark.entity.ScanTool;
import com.vulnark.repository.ScanToolRepository;
import com.vulnark.service.ToolDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 工具下载服务实现类
 */
@Service
public class ToolDownloadServiceImpl implements ToolDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(ToolDownloadServiceImpl.class);

    @Value("${vulnark.tools.base-path:${user.home}/.vulnark/tools}")
    private String toolsBasePath;
    
    @Value("${vulnark.tools.download-timeout:300000}")
    private int downloadTimeout;
    
    @Autowired
    private ScanToolRepository scanToolRepository;

    // 工具官方下载页面
    private static final Map<String, String> TOOL_WEBSITES = Map.of(
            "nuclei", "https://github.com/projectdiscovery/nuclei/releases",
            "xray", "https://github.com/chaitin/xray/releases",
            "nessus", "https://www.tenable.com/downloads/nessus",
            "awvs", "https://www.acunetix.com/vulnerability-scanner/download/"
    );

    // 工具下载链接（示例）
    private static final Map<String, Map<String, String>> DOWNLOAD_URLS = Map.of(
            "nuclei", Map.of(
                    "linux-amd64", "https://github.com/projectdiscovery/nuclei/releases/download/v2.9.4/nuclei_2.9.4_linux_amd64.zip",
                    "linux-arm64", "https://github.com/projectdiscovery/nuclei/releases/download/v2.9.4/nuclei_2.9.4_linux_arm64.zip",
                    "windows-amd64", "https://github.com/projectdiscovery/nuclei/releases/download/v2.9.4/nuclei_2.9.4_windows_amd64.zip",
                    "darwin-amd64", "https://github.com/projectdiscovery/nuclei/releases/download/v2.9.4/nuclei_2.9.4_macOS_amd64.zip",
                    "darwin-arm64", "https://github.com/projectdiscovery/nuclei/releases/download/v2.9.4/nuclei_2.9.4_macOS_arm64.zip"
            ),
            "xray", Map.of(
                    "linux-amd64", "https://github.com/chaitin/xray/releases/download/1.9.4/xray_linux_amd64.zip",
                    "linux-arm64", "https://github.com/chaitin/xray/releases/download/1.9.4/xray_linux_arm64.zip",
                    "windows-amd64", "https://github.com/chaitin/xray/releases/download/1.9.4/xray_windows_amd64.zip",
                    "darwin-amd64", "https://github.com/chaitin/xray/releases/download/1.9.4/xray_darwin_amd64.zip",
                    "darwin-arm64", "https://github.com/chaitin/xray/releases/download/1.9.4/xray_darwin_arm64.zip"
            )
    );

    @Override
    public List<Map<String, Object>> getAvailableTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        // Nuclei
        tools.add(Map.of(
                "id", "nuclei",
                "name", "Nuclei",
                "description", "快速、可定制的漏洞扫描器",
                "website", TOOL_WEBSITES.get("nuclei"),
                "versions", List.of("2.9.4", "2.9.3", "2.9.2"),
                "platforms", List.of("linux", "windows", "darwin"),
                "architectures", List.of("amd64", "arm64")
        ));
        
        // Xray
        tools.add(Map.of(
                "id", "xray",
                "name", "Xray",
                "description", "安全评估工具，支持主动、被动扫描",
                "website", TOOL_WEBSITES.get("xray"),
                "versions", List.of("1.9.4", "1.9.3", "1.9.2"),
                "platforms", List.of("linux", "windows", "darwin"),
                "architectures", List.of("amd64", "arm64")
        ));
        
        // Nessus
        tools.add(Map.of(
                "id", "nessus",
                "name", "Nessus",
                "description", "专业漏洞扫描工具",
                "website", TOOL_WEBSITES.get("nessus"),
                "versions", List.of("latest"),
                "platforms", List.of("linux", "windows"),
                "architectures", List.of("amd64")
        ));
        
        // AWVS
        tools.add(Map.of(
                "id", "awvs",
                "name", "AWVS",
                "description", "Web应用漏洞扫描工具",
                "website", TOOL_WEBSITES.get("awvs"),
                "versions", List.of("latest"),
                "platforms", List.of("linux", "windows"),
                "architectures", List.of("amd64")
        ));
        
        return tools;
    }

    @Override
    public String getDownloadUrl(String toolType, String version, String platform, String arch) {
        // 对于Nessus和AWVS，直接返回官网链接
        if ("nessus".equals(toolType) || "awvs".equals(toolType)) {
            return TOOL_WEBSITES.get(toolType);
        }
        
        // 对于其他工具，返回对应的下载链接
        Map<String, String> urls = DOWNLOAD_URLS.get(toolType);
        if (urls != null) {
            String key = platform + "-" + arch;
            String url = urls.get(key);
            if (url != null) {
                return url;
            }
        }
        
        // 如果没有找到对应的下载链接，返回官网链接
        return TOOL_WEBSITES.getOrDefault(toolType, "");
    }

    @Override
    public Map<String, Object> checkToolStatus(String toolType) {
        File toolDir = new File(toolsBasePath, toolType);
        boolean installed = toolDir.exists() && toolDir.isDirectory();
        
        Map<String, Object> status = new HashMap<>();
        status.put("toolType", toolType);
        status.put("installed", installed);
        
        if (installed) {
            // 这里可以添加更多的状态信息，如版本、安装路径等
            status.put("installPath", toolDir.getAbsolutePath());
            status.put("lastModified", new Date(toolDir.lastModified()));
        }
        
        return status;
    }
    
    /**
     * 异步下载和安装工具
     */
    @Override
    @Async("downloadTaskExecutor")
    public void downloadAndInstallTool(ScanTool tool) {
        logger.info("开始下载工具: {}", tool.getName());
        
        try {
            // 创建工具目录
            String toolDir = toolsBasePath + "/" + tool.getName();
            createDirectoryIfNotExists(toolDir);
            
            // 下载文件
            String downloadPath = toolDir + "/download.zip";
            downloadFile(tool.getDownloadUrl(), downloadPath, tool.getName());
            
            // 解压文件
            extractZipFile(downloadPath, toolDir);
            
            // 设置执行权限
            setExecutablePermission(tool.getInstallPath());
            
            // 创建默认配置文件
            createDefaultConfig(tool);
            
            // 更新工具状态
            updateToolStatus(tool, ScanTool.ToolStatus.INSTALLED, null);
            
            // 删除下载的zip文件
            Files.deleteIfExists(Paths.get(downloadPath));
            
            logger.info("工具 {} 安装完成", tool.getName());
            
        } catch (Exception e) {
            logger.error("下载安装工具 {} 失败: {}", tool.getName(), e.getMessage(), e);
            updateToolStatus(tool, ScanTool.ToolStatus.ERROR, e.getMessage());
        }
    }
    
    /**
     * 下载文件
     */
    private void downloadFile(String downloadUrl, String savePath, String toolName) throws IOException {
        logger.info("开始下载文件: {} -> {}", downloadUrl, savePath);
        
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(downloadTimeout);
        connection.setRequestProperty("User-Agent", "VulnArk/1.0");
        
        // 获取文件大小
        long fileSize = connection.getContentLengthLong();
        logger.info("文件大小: {} bytes", fileSize);
        
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(savePath)) {
            
            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            int bytesRead;
            int lastProgress = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // 更新下载进度
                if (fileSize > 0) {
                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                    if (progress != lastProgress && progress % 10 == 0) {
                        updateDownloadProgress(toolName, progress);
                        logger.info("下载进度: {}%", progress);
                        lastProgress = progress;
                    }
                }
            }

            updateDownloadProgress(toolName, 100);
            logger.info("文件下载完成: {}", savePath);
        }
    }
    
    /**
     * 解压ZIP文件
     */
    private void extractZipFile(String zipFilePath, String destDir) throws IOException {
        logger.info("开始解压文件: {} -> {}", zipFilePath, destDir);
        
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                Path entryPath = Paths.get(destDir, entryName);
                
                // 安全检查：防止路径遍历攻击
                if (!entryPath.normalize().startsWith(Paths.get(destDir).normalize())) {
                    throw new IOException("不安全的ZIP条目: " + entryName);
                }
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    // 确保父目录存在
                    Files.createDirectories(entryPath.getParent());
                    
                    // 提取文件
                    try (FileOutputStream outputStream = new FileOutputStream(entryPath.toFile())) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
                
                zipInputStream.closeEntry();
            }
        }
        
        logger.info("文件解压完成");
    }
    
    /**
     * 设置文件执行权限
     */
    private void setExecutablePermission(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean success = file.setExecutable(true, false);
                if (success) {
                    logger.info("设置执行权限成功: {}", filePath);
                } else {
                    logger.warn("设置执行权限失败: {}", filePath);
                }
            } else {
                logger.warn("文件不存在，无法设置执行权限: {}", filePath);
            }
        } catch (Exception e) {
            logger.error("设置执行权限失败: {}", e.getMessage());
        }
    }
    
    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig(ScanTool tool) {
        try {
            String configContent = generateDefaultConfig(tool.getName());
            if (configContent != null) {
                Path configPath = Paths.get(tool.getConfigPath());
                Files.createDirectories(configPath.getParent());
                Files.write(configPath, configContent.getBytes());
                logger.info("创建默认配置文件: {}", tool.getConfigPath());
            }
        } catch (Exception e) {
            logger.error("创建配置文件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 生成默认配置内容
     */
    private String generateDefaultConfig(String toolName) {
        switch (toolName) {
            case "xray":
                return generateXrayConfig();
            case "nuclei":
                return generateNucleiConfig();
            default:
                return null;
        }
    }
    
    /**
     * 生成Xray默认配置
     */
    private String generateXrayConfig() {
        return """
                # Xray 配置文件
                version: 2.0
                
                # 基础配置
                basic:
                  # 并发数
                  thread: 30
                  # 超时时间
                  timeout: 30s
                  # 最大重试次数
                  max_retry: 3
                
                # HTTP配置
                http:
                  # 代理配置
                  proxy: ""
                  # 用户代理
                  user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                  # 超时时间
                  timeout: 10s
                  # 最大重定向次数
                  max_redirect: 5
                
                # 插件配置
                plugins:
                  # 启用的插件
                  enabled:
                    - baseline
                    - brute-force
                    - cmd-injection
                    - crlf-injection
                    - dirscan
                    - path-traversal
                    - redirect
                    - sql-injection
                    - ssrf
                    - xss
                    - xxe
                
                # 输出配置
                output:
                  # 输出格式
                  format: json
                  # 输出文件
                  filename: "scan_result.json"
                """;
    }
    
    /**
     * 生成Nuclei默认配置
     */
    private String generateNucleiConfig() {
        return """
                # Nuclei 配置文件
                
                # 基础配置
                threads: 25
                timeout: 10
                retries: 1
                rate-limit: 150
                
                # 模板配置
                templates:
                  - cves/
                  - vulnerabilities/
                  - exposures/
                  - misconfiguration/
                  - default-logins/
                
                # 输出配置
                output: scan_result.json
                json: true
                
                # 其他配置
                follow-redirects: true
                max-redirects: 10
                disable-clustering: false
                """;
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            logger.info("创建目录: {}", dirPath);
        }
    }

    /**
     * 更新工具状态
     */
    private void updateToolStatus(ScanTool tool, ScanTool.ToolStatus status, String errorMessage) {
        tool.setStatus(status);
        tool.setErrorMessage(errorMessage);

        if (status == ScanTool.ToolStatus.INSTALLED) {
            tool.setLastUpdateTime(LocalDateTime.now());
            tool.setCurrentVersion(tool.getLatestVersion());
        }

        scanToolRepository.save(tool);
    }

    /**
     * 更新下载进度
     */
    private void updateDownloadProgress(String toolName, int progress) {
        Optional<ScanTool> toolOpt = scanToolRepository.findByName(toolName);
        if (toolOpt.isPresent()) {
            ScanTool tool = toolOpt.get();
            tool.setDownloadProgress(progress);
            scanToolRepository.save(tool);
        }
    }
} 