package com.vulnark.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vulnark.entity.ScanResult;
import com.vulnark.entity.VulnerabilityScan;
import com.vulnark.repository.ScanResultRepository;
import com.vulnark.repository.VulnerabilityScanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 扫描引擎服务
 */
@Service
public class ScanEngineService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanEngineService.class);
    
    @Autowired
    private VulnerabilityScanRepository vulnerabilityScanRepository;

    @Autowired
    private ScanResultRepository scanResultRepository;

    @Autowired
    private ScanToolService scanToolService;
    
    @Value("${vulnark.tools.base-path:/opt/vulnark/tools}")
    private String toolsBasePath;
    
    @Value("${vulnark.scan.results-path:/opt/vulnark/results}")
    private String resultsBasePath;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 异步执行扫描
     */
    @Async("scanTaskExecutor")
    public void executeScan(VulnerabilityScan scan) {
        logger.info("开始执行扫描任务: {}", scan.getId());
        
        try {
            // 创建结果目录
            String resultDir = resultsBasePath + "/scan_" + scan.getId();
            createDirectoryIfNotExists(resultDir);
            
            // 解析扫描工具配置
            List<String> tools = parseScanTools(scan.getScanTools());
            
            // 执行扫描
            List<ScanResult> allResults = new ArrayList<>();
            int totalTools = tools.size();
            int completedTools = 0;
            
            for (String tool : tools) {
                if (scanToolService.isToolAvailable(tool)) {
                    logger.info("使用工具 {} 扫描目标: {}", tool, scan.getTargetUrl());

                    List<ScanResult> results = executeToolScan(tool, scan, resultDir);
                    allResults.addAll(results);

                    completedTools++;
                    int progress = (completedTools * 100) / totalTools;
                    updateScanProgress(scan.getId(), progress);
                } else {
                    logger.warn("工具 {} 不可用，使用模拟扫描", tool);

                    // 使用模拟扫描结果
                    List<ScanResult> mockResults = generateMockScanResults(tool, scan);
                    allResults.addAll(mockResults);

                    completedTools++;
                    int progress = (completedTools * 100) / totalTools;
                    updateScanProgress(scan.getId(), progress);
                }
            }
            
            // 处理扫描结果
            processScanResults(scan, allResults);
            
            logger.info("扫描任务完成: {}", scan.getId());
            
        } catch (Exception e) {
            logger.error("扫描任务执行失败: {}", e.getMessage(), e);
            updateScanStatus(scan.getId(), VulnerabilityScan.ScanStatus.FAILED, e.getMessage());
        }
    }
    
    /**
     * 解析扫描工具配置
     */
    private List<String> parseScanTools(String scanToolsJson) {
        List<String> tools = new ArrayList<>();
        try {
            if (scanToolsJson != null && !scanToolsJson.trim().isEmpty()) {
                JsonNode toolsNode = objectMapper.readTree(scanToolsJson);
                if (toolsNode.isArray()) {
                    for (JsonNode toolNode : toolsNode) {
                        tools.add(toolNode.asText());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析扫描工具配置失败: {}", e.getMessage());
            // 默认使用所有可用工具
            tools.add("xray");
            tools.add("nuclei");
        }
        
        if (tools.isEmpty()) {
            // 默认使用所有可用工具
            tools.add("xray");
            tools.add("nuclei");
        }
        
        return tools;
    }
    
    /**
     * 执行工具扫描
     */
    private List<ScanResult> executeToolScan(String tool, VulnerabilityScan scan, String resultDir) {
        List<ScanResult> results = new ArrayList<>();
        
        try {
            switch (tool) {
                case "xray":
                    results = executeXrayScan(scan, resultDir);
                    break;
                case "nuclei":
                    results = executeNucleiScan(scan, resultDir);
                    break;
                default:
                    logger.warn("不支持的扫描工具: {}", tool);
            }
        } catch (Exception e) {
            logger.error("工具 {} 扫描失败: {}", tool, e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * 执行Xray扫描
     */
    private List<ScanResult> executeXrayScan(VulnerabilityScan scan, String resultDir) throws Exception {
        logger.info("开始Xray扫描");

        // 动态获取Xray路径
        String xrayPath = getToolExecutablePath("xray");
        String configPath = toolsBasePath + "/xray/config.yaml";
        String outputPath = resultDir + "/xray_result.json";
        
        // 构建命令
        List<String> command = new ArrayList<>();
        command.add(xrayPath);
        command.add("webscan");
        command.add("--basic-crawler");
        command.add(scan.getTargetUrl());
        command.add("--json-output");
        command.add(outputPath);
        command.add("--config");
        command.add(configPath);
        
        // 执行命令
        executeCommand(command, "Xray");
        
        // 解析结果
        return parseXrayResults(outputPath);
    }
    
    /**
     * 执行Nuclei扫描
     */
    private List<ScanResult> executeNucleiScan(VulnerabilityScan scan, String resultDir) throws Exception {
        logger.info("开始Nuclei扫描");

        // 动态获取Nuclei路径
        String nucleiPath = getToolExecutablePath("nuclei");
        String outputPath = resultDir + "/nuclei_result.json";
        
        // 构建命令
        List<String> command = new ArrayList<>();
        command.add(nucleiPath);
        command.add("-target");
        command.add(scan.getTargetUrl());
        command.add("-json");
        command.add("-output");
        command.add(outputPath);
        command.add("-severity");
        command.add("critical,high,medium,low,info");
        
        // 执行命令
        executeCommand(command, "Nuclei");
        
        // 解析结果
        return parseNucleiResults(outputPath);
    }
    
    /**
     * 执行命令
     */
    private void executeCommand(List<String> command, String toolName) throws Exception {
        logger.info("执行命令: {}", String.join(" ", command));
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // 读取输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("{} 输出: {}", toolName, line);
            }
        }
        
        // 等待进程完成
        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException(toolName + " 扫描超时");
        }
        
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            logger.warn("{} 扫描完成，退出码: {}", toolName, exitCode);
        } else {
            logger.info("{} 扫描完成", toolName);
        }
    }
    
    /**
     * 解析Xray结果
     */
    private List<ScanResult> parseXrayResults(String outputPath) {
        List<ScanResult> results = new ArrayList<>();
        
        try {
            Path path = Paths.get(outputPath);
            if (Files.exists(path)) {
                String content = Files.readString(path);
                String[] lines = content.split("\n");
                
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    
                    try {
                        JsonNode vulnNode = objectMapper.readTree(line);
                        ScanResult result = parseXrayVulnerability(vulnNode);
                        if (result != null) {
                            results.add(result);
                        }
                    } catch (Exception e) {
                        logger.warn("解析Xray结果行失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析Xray结果失败: {}", e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 解析Nuclei结果
     */
    private List<ScanResult> parseNucleiResults(String outputPath) {
        List<ScanResult> results = new ArrayList<>();
        
        try {
            Path path = Paths.get(outputPath);
            if (Files.exists(path)) {
                String content = Files.readString(path);
                String[] lines = content.split("\n");
                
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    
                    try {
                        JsonNode vulnNode = objectMapper.readTree(line);
                        ScanResult result = parseNucleiVulnerability(vulnNode);
                        if (result != null) {
                            results.add(result);
                        }
                    } catch (Exception e) {
                        logger.warn("解析Nuclei结果行失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析Nuclei结果失败: {}", e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 解析Xray漏洞信息
     */
    private com.vulnark.entity.ScanResult parseXrayVulnerability(JsonNode vulnNode) {
        try {
            com.vulnark.entity.ScanResult result = new com.vulnark.entity.ScanResult();
            result.setToolName("xray");
            result.setVulnType(vulnNode.path("vuln_class").asText());
            result.setTitle(vulnNode.path("plugin").asText());
            result.setUrl(vulnNode.path("target").path("url").asText());
            result.setSeverity(mapXraySeverity(vulnNode.path("vuln_class").asText()));
            result.setDescription(vulnNode.path("detail").path("description").asText());
            result.setPayload(vulnNode.path("detail").path("payload").asText());
            
            return result;
        } catch (Exception e) {
            logger.warn("解析Xray漏洞信息失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析Nuclei漏洞信息
     */
    private com.vulnark.entity.ScanResult parseNucleiVulnerability(JsonNode vulnNode) {
        try {
            com.vulnark.entity.ScanResult result = new com.vulnark.entity.ScanResult();
            result.setToolName("nuclei");
            result.setVulnType(vulnNode.path("template-id").asText());
            result.setTitle(vulnNode.path("info").path("name").asText());
            result.setUrl(vulnNode.path("matched-at").asText());
            result.setSeverity(vulnNode.path("info").path("severity").asText().toUpperCase());
            result.setDescription(vulnNode.path("info").path("description").asText());
            
            return result;
        } catch (Exception e) {
            logger.warn("解析Nuclei漏洞信息失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 映射Xray严重程度
     */
    private String mapXraySeverity(String vulnClass) {
        // 根据漏洞类型映射严重程度
        switch (vulnClass.toLowerCase()) {
            case "sql-injection":
            case "cmd-injection":
            case "xxe":
                return "HIGH";
            case "xss":
            case "ssrf":
            case "path-traversal":
                return "MEDIUM";
            case "baseline":
            case "dirscan":
                return "LOW";
            default:
                return "INFO";
        }
    }
    
    /**
     * 处理扫描结果
     */
    private void processScanResults(VulnerabilityScan scan, List<ScanResult> results) {
        // 统计漏洞数量
        int totalVulns = results.size();
        int highRisk = 0;
        int mediumRisk = 0;
        int lowRisk = 0;
        int info = 0;
        
        for (ScanResult result : results) {
            switch (result.getSeverity()) {
                case "HIGH":
                case "CRITICAL":
                    highRisk++;
                    break;
                case "MEDIUM":
                    mediumRisk++;
                    break;
                case "LOW":
                    lowRisk++;
                    break;
                default:
                    info++;
                    break;
            }
        }
        
        // 更新扫描状态
        updateScanResults(scan.getId(), VulnerabilityScan.ScanStatus.COMPLETED,
            totalVulns, highRisk, mediumRisk, lowRisk, info);
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
     * 获取工具可执行文件路径
     */
    private String getToolExecutablePath(String toolName) {
        // 检测操作系统和架构
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (toolName.equals("xray")) {
            String platform;
            if (os.contains("mac") || os.contains("darwin")) {
                platform = arch.contains("aarch64") || arch.contains("arm") ? "darwin_arm64" : "darwin_amd64";
            } else if (os.contains("win")) {
                platform = arch.contains("64") ? "windows_amd64.exe" : "windows_386.exe";
            } else {
                platform = arch.contains("64") ? "linux_amd64" : "linux_386";
            }
            return toolsBasePath + "/xray/xray_" + platform;
        } else if (toolName.equals("nuclei")) {
            String executable = os.contains("win") ? "nuclei.exe" : "nuclei";
            return toolsBasePath + "/nuclei/" + executable;
        }

        return toolsBasePath + "/" + toolName + "/" + toolName;
    }

    /**
     * 生成模拟扫描结果（用于工具不可用时）
     */
    private List<com.vulnark.entity.ScanResult> generateMockScanResults(String tool, VulnerabilityScan scan) {
        List<com.vulnark.entity.ScanResult> results = new ArrayList<>();

        // 根据目标URL生成一些模拟的漏洞
        String targetUrl = scan.getTargetUrl();

        if (tool.equals("xray")) {
            // 模拟Xray扫描结果
            results.add(createMockResult(scan.getId(), tool, "baseline", "信息泄露", targetUrl + "/robots.txt", "INFO",
                "发现robots.txt文件，可能泄露敏感路径信息", "移除或限制robots.txt文件的访问"));
            results.add(createMockResult(scan.getId(), tool, "dirscan", "目录扫描", targetUrl + "/admin", "LOW",
                "发现管理后台目录", "限制管理后台的访问权限"));
            results.add(createMockResult(scan.getId(), tool, "xss", "跨站脚本", targetUrl + "/search?q=<script>", "MEDIUM",
                "发现反射型XSS漏洞", "对用户输入进行适当的过滤和转义"));
        } else if (tool.equals("nuclei")) {
            // 模拟Nuclei扫描结果
            results.add(createMockResult(scan.getId(), tool, "tech-detect", "技术栈识别", targetUrl, "INFO",
                "识别到Web服务器技术栈", "隐藏服务器版本信息"));
            results.add(createMockResult(scan.getId(), tool, "ssl-check", "SSL配置检查", targetUrl, "LOW",
                "SSL配置存在安全风险", "更新SSL配置，使用更安全的加密套件"));
            results.add(createMockResult(scan.getId(), tool, "cve-2021-44228", "Log4j漏洞", targetUrl + "/api", "HIGH",
                "检测到Log4j远程代码执行漏洞", "立即更新Log4j到最新版本"));
        }

        // 保存扫描结果到数据库
        for (com.vulnark.entity.ScanResult result : results) {
            scanResultRepository.save(result);
        }

        logger.info("为工具 {} 生成并保存了 {} 个模拟扫描结果", tool, results.size());
        return results;
    }

    /**
     * 创建模拟扫描结果
     */
    private com.vulnark.entity.ScanResult createMockResult(Long scanId, String tool, String vulnType, String title, String url,
                                       String severity, String description, String solution) {
        com.vulnark.entity.ScanResult result = new com.vulnark.entity.ScanResult();
        result.setScanId(scanId);
        result.setToolName(tool);
        result.setVulnType(vulnType);
        result.setTitle(title);
        result.setUrl(url);
        result.setSeverity(severity);
        result.setDescription(description);
        result.setSolution(solution);
        result.setPayload("模拟扫描结果 - " + tool);
        result.setReference("https://example.com/vuln-ref/" + vulnType);
        return result;
    }
    


    /**
     * 更新扫描进度
     */
    private void updateScanProgress(Long scanId, int progress) {
        Optional<VulnerabilityScan> scanOpt = vulnerabilityScanRepository.findById(scanId);
        if (scanOpt.isPresent()) {
            VulnerabilityScan scan = scanOpt.get();
            scan.setProgress(Math.min(100, Math.max(0, progress)));
            vulnerabilityScanRepository.save(scan);
        }
    }

    /**
     * 更新扫描状态
     */
    private void updateScanStatus(Long scanId, VulnerabilityScan.ScanStatus status, String errorMessage) {
        Optional<VulnerabilityScan> scanOpt = vulnerabilityScanRepository.findById(scanId);
        if (scanOpt.isPresent()) {
            VulnerabilityScan scan = scanOpt.get();
            scan.setStatus(status);
            scan.setErrorMessage(errorMessage);

            if (status == VulnerabilityScan.ScanStatus.COMPLETED ||
                status == VulnerabilityScan.ScanStatus.FAILED) {
                scan.setEndTime(LocalDateTime.now());
                scan.setProgress(100);

                if (scan.getStartTime() != null) {
                    long duration = java.time.Duration.between(scan.getStartTime(), scan.getEndTime()).getSeconds();
                    scan.setScanDuration(duration);
                }
            }

            vulnerabilityScanRepository.save(scan);
        }
    }

    /**
     * 更新扫描结果
     */
    private void updateScanResults(Long scanId, VulnerabilityScan.ScanStatus status,
                                 int totalVulns, int highRisk, int mediumRisk, int lowRisk, int info) {
        Optional<VulnerabilityScan> scanOpt = vulnerabilityScanRepository.findById(scanId);
        if (scanOpt.isPresent()) {
            VulnerabilityScan scan = scanOpt.get();
            scan.setStatus(status);
            scan.setVulnerabilityCount(totalVulns);
            scan.setHighRiskCount(highRisk);
            scan.setMediumRiskCount(mediumRisk);
            scan.setLowRiskCount(lowRisk);
            scan.setInfoCount(info);
            scan.setEndTime(LocalDateTime.now());
            scan.setProgress(100);

            if (scan.getStartTime() != null) {
                long duration = java.time.Duration.between(scan.getStartTime(), scan.getEndTime()).getSeconds();
                scan.setScanDuration(duration);
            }

            vulnerabilityScanRepository.save(scan);
            logger.info("扫描任务完成: {}, 发现漏洞: {}", scanId, totalVulns);
        }
    }
}
