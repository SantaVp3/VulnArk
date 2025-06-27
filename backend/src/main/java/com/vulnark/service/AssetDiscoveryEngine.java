package com.vulnark.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vulnark.entity.AssetDiscoveryResult;
import com.vulnark.entity.AssetDiscoveryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * 资产发现引擎
 * 负责执行网络扫描和服务检测
 */
@Service
public class AssetDiscoveryEngine {

    private static final Logger logger = LoggerFactory.getLogger(AssetDiscoveryEngine.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 常用端口列表
    private static final int[] COMMON_PORTS = {
        21, 22, 23, 25, 53, 80, 110, 143, 443, 993, 995,
        135, 139, 445, 1433, 1521, 3306, 3389, 5432, 5900,
        6379, 8080, 8443, 9200, 27017
    };

    // 服务指纹识别规则
    private static final Map<Integer, String> PORT_SERVICE_MAP = new HashMap<>();
    static {
        PORT_SERVICE_MAP.put(21, "FTP");
        PORT_SERVICE_MAP.put(22, "SSH");
        PORT_SERVICE_MAP.put(23, "Telnet");
        PORT_SERVICE_MAP.put(25, "SMTP");
        PORT_SERVICE_MAP.put(53, "DNS");
        PORT_SERVICE_MAP.put(80, "HTTP");
        PORT_SERVICE_MAP.put(110, "POP3");
        PORT_SERVICE_MAP.put(143, "IMAP");
        PORT_SERVICE_MAP.put(443, "HTTPS");
        PORT_SERVICE_MAP.put(993, "IMAPS");
        PORT_SERVICE_MAP.put(995, "POP3S");
        PORT_SERVICE_MAP.put(135, "RPC");
        PORT_SERVICE_MAP.put(139, "NetBIOS");
        PORT_SERVICE_MAP.put(445, "SMB");
        PORT_SERVICE_MAP.put(1433, "MSSQL");
        PORT_SERVICE_MAP.put(1521, "Oracle");
        PORT_SERVICE_MAP.put(3306, "MySQL");
        PORT_SERVICE_MAP.put(3389, "RDP");
        PORT_SERVICE_MAP.put(5432, "PostgreSQL");
        PORT_SERVICE_MAP.put(5900, "VNC");
        PORT_SERVICE_MAP.put(6379, "Redis");
        PORT_SERVICE_MAP.put(8080, "HTTP-Alt");
        PORT_SERVICE_MAP.put(8443, "HTTPS-Alt");
        PORT_SERVICE_MAP.put(9200, "Elasticsearch");
        PORT_SERVICE_MAP.put(27017, "MongoDB");
    }

    /**
     * 执行主机发现扫描
     */
    public List<AssetDiscoveryResult> performHostDiscovery(AssetDiscoveryTask task) {
        logger.info("开始执行主机发现任务: {}", task.getName());
        List<AssetDiscoveryResult> results = new ArrayList<>();
        
        try {
            List<String> targets = parseTargets(task.getTargets(), task.getTargetType());
            
            for (String target : targets) {
                AssetDiscoveryResult result = pingHost(task.getId(), target);
                if (result != null) {
                    results.add(result);
                }
            }
            
        } catch (Exception e) {
            logger.error("主机发现扫描失败: {}", e.getMessage(), e);
        }
        
        logger.info("主机发现完成，发现 {} 个主机", results.size());
        return results;
    }

    /**
     * 执行端口扫描
     */
    public List<AssetDiscoveryResult> performPortScan(AssetDiscoveryTask task) {
        logger.info("开始执行端口扫描任务: {}", task.getName());
        List<AssetDiscoveryResult> results = new ArrayList<>();
        
        try {
            List<String> targets = parseTargets(task.getTargets(), task.getTargetType());
            int[] ports = parsePorts(task.getScanPorts());
            
            List<CompletableFuture<AssetDiscoveryResult>> futures = new ArrayList<>();
            
            for (String target : targets) {
                CompletableFuture<AssetDiscoveryResult> future = CompletableFuture.supplyAsync(() -> {
                    return scanHost(task.getId(), target, ports);
                }, executorService);
                futures.add(future);
            }
            
            // 等待所有扫描完成
            for (CompletableFuture<AssetDiscoveryResult> future : futures) {
                AssetDiscoveryResult result = future.get();
                if (result != null) {
                    results.add(result);
                }
            }
            
        } catch (Exception e) {
            logger.error("端口扫描失败: {}", e.getMessage(), e);
        }
        
        logger.info("端口扫描完成，扫描了 {} 个主机", results.size());
        return results;
    }

    /**
     * 执行服务检测
     */
    public List<AssetDiscoveryResult> performServiceDetection(AssetDiscoveryTask task) {
        logger.info("开始执行服务检测任务: {}", task.getName());
        List<AssetDiscoveryResult> results = new ArrayList<>();
        
        try {
            List<String> targets = parseTargets(task.getTargets(), task.getTargetType());
            int[] ports = parsePorts(task.getScanPorts());
            
            for (String target : targets) {
                AssetDiscoveryResult result = detectServices(task.getId(), target, ports);
                if (result != null) {
                    results.add(result);
                }
            }
            
        } catch (Exception e) {
            logger.error("服务检测失败: {}", e.getMessage(), e);
        }
        
        logger.info("服务检测完成，检测了 {} 个主机", results.size());
        return results;
    }

    /**
     * 执行全面扫描
     */
    public List<AssetDiscoveryResult> performFullScan(AssetDiscoveryTask task) {
        logger.info("开始执行全面扫描任务: {}", task.getName());
        List<AssetDiscoveryResult> results = new ArrayList<>();
        
        // 先执行主机发现
        List<AssetDiscoveryResult> hostResults = performHostDiscovery(task);
        
        // 对在线主机执行端口扫描和服务检测
        for (AssetDiscoveryResult hostResult : hostResults) {
            if (hostResult.getIsAlive()) {
                try {
                    int[] ports = parsePorts(task.getScanPorts());
                    AssetDiscoveryResult detailedResult = detectServices(task.getId(), 
                                                                        hostResult.getIpAddress(), ports);
                    if (detailedResult != null) {
                        // 合并主机发现和服务检测结果
                        mergeResults(hostResult, detailedResult);
                        results.add(hostResult);
                    }
                } catch (Exception e) {
                    logger.error("全面扫描主机 {} 失败: {}", hostResult.getIpAddress(), e.getMessage());
                    results.add(hostResult); // 仍然添加基本的主机信息
                }
            }
        }
        
        logger.info("全面扫描完成，扫描了 {} 个主机", results.size());
        return results;
    }

    /**
     * Ping主机检测
     */
    private AssetDiscoveryResult pingHost(Long taskId, String target) {
        try {
            InetAddress address = InetAddress.getByName(target);
            long startTime = System.currentTimeMillis();
            boolean reachable = address.isReachable(5000); // 5秒超时
            long endTime = System.currentTimeMillis();
            
            AssetDiscoveryResult result = new AssetDiscoveryResult(taskId, target, address.getHostAddress());
            result.setIsAlive(reachable);
            result.setResponseTime((int)(endTime - startTime));
            result.setHostname(address.getHostName());
            
            if (reachable) {
                logger.debug("主机 {} 在线，响应时间: {}ms", target, result.getResponseTime());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.debug("Ping主机 {} 失败: {}", target, e.getMessage());
            return null;
        }
    }

    /**
     * 扫描主机端口
     */
    private AssetDiscoveryResult scanHost(Long taskId, String target, int[] ports) {
        try {
            InetAddress address = InetAddress.getByName(target);
            AssetDiscoveryResult result = new AssetDiscoveryResult(taskId, target, address.getHostAddress());
            
            List<Integer> openPorts = new ArrayList<>();
            Map<String, Object> services = new HashMap<>();
            
            for (int port : ports) {
                if (isPortOpen(address.getHostAddress(), port, 3000)) {
                    openPorts.add(port);
                    String service = PORT_SERVICE_MAP.get(port);
                    if (service != null) {
                        services.put(String.valueOf(port), service);
                    }
                }
            }
            
            result.setIsAlive(!openPorts.isEmpty());
            result.setOpenPorts(objectMapper.writeValueAsString(openPorts));
            result.setServices(objectMapper.writeValueAsString(services));
            result.setHostname(address.getHostName());
            
            return result;
            
        } catch (Exception e) {
            logger.debug("扫描主机 {} 失败: {}", target, e.getMessage());
            return null;
        }
    }

    /**
     * 检测服务
     */
    private AssetDiscoveryResult detectServices(Long taskId, String target, int[] ports) {
        AssetDiscoveryResult result = scanHost(taskId, target, ports);
        if (result != null && result.getIsAlive()) {
            // 尝试操作系统检测
            String os = detectOperatingSystem(target);
            if (os != null) {
                result.setOperatingSystem(os);
            }
            
            // 设置设备类型和厂商信息
            result.setDeviceType(guessDeviceType(result));
            result.setVendor(guessVendor(result));
            result.setConfidenceScore(calculateConfidenceScore(result));
        }
        return result;
    }

    /**
     * 检查端口是否开放
     */
    private boolean isPortOpen(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 解析扫描目标
     */
    private List<String> parseTargets(String targets, AssetDiscoveryTask.TargetType targetType) {
        List<String> targetList = new ArrayList<>();
        
        try {
            switch (targetType) {
                case IP_RANGE:
                    targetList.addAll(parseIpRange(targets));
                    break;
                case SUBNET:
                    targetList.addAll(parseSubnet(targets));
                    break;
                case DOMAIN:
                    targetList.add(targets);
                    break;
                case URL_LIST:
                    String[] urls = targets.split("[,\\n]");
                    for (String url : urls) {
                        targetList.add(url.trim());
                    }
                    break;
                case CUSTOM:
                    // 解析JSON格式的自定义目标
                    targetList.addAll(parseCustomTargets(targets));
                    break;
            }
        } catch (Exception e) {
            logger.error("解析扫描目标失败: {}", e.getMessage());
        }
        
        return targetList;
    }

    /**
     * 解析IP范围
     */
    private List<String> parseIpRange(String range) {
        List<String> ips = new ArrayList<>();
        // 支持格式: 192.168.1.1-192.168.1.100
        if (range.contains("-")) {
            String[] parts = range.split("-");
            if (parts.length == 2) {
                String startIp = parts[0].trim();
                String endIp = parts[1].trim();
                ips.addAll(generateIpRange(startIp, endIp));
            }
        } else {
            ips.add(range);
        }
        return ips;
    }

    /**
     * 解析子网
     */
    private List<String> parseSubnet(String subnet) {
        List<String> ips = new ArrayList<>();
        // 支持CIDR格式: 192.168.1.0/24
        if (subnet.contains("/")) {
            ips.addAll(generateSubnetIps(subnet));
        } else {
            ips.add(subnet);
        }
        return ips;
    }

    /**
     * 解析自定义目标
     */
    private List<String> parseCustomTargets(String customTargets) {
        List<String> targets = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<String> targetList = objectMapper.readValue(customTargets, List.class);
            targets.addAll(targetList);
        } catch (JsonProcessingException e) {
            logger.error("解析自定义目标失败: {}", e.getMessage());
        }
        return targets;
    }

    /**
     * 解析端口配置
     */
    private int[] parsePorts(String portConfig) {
        if (portConfig == null || portConfig.trim().isEmpty()) {
            return COMMON_PORTS;
        }
        
        List<Integer> ports = new ArrayList<>();
        String[] parts = portConfig.split(",");
        
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                // 端口范围: 80-90
                String[] range = part.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        for (int i = start; i <= end; i++) {
                            ports.add(i);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("无效的端口范围: {}", part);
                    }
                }
            } else {
                // 单个端口
                try {
                    ports.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    logger.warn("无效的端口号: {}", part);
                }
            }
        }
        
        return ports.stream().mapToInt(Integer::intValue).toArray();
    }

    // 其他辅助方法的占位实现
    private List<String> generateIpRange(String startIp, String endIp) {
        // 简化实现，实际应该根据IP地址生成范围
        List<String> ips = new ArrayList<>();
        ips.add(startIp);
        ips.add(endIp);
        return ips;
    }

    private List<String> generateSubnetIps(String subnet) {
        // 简化实现，实际应该根据CIDR生成IP列表
        List<String> ips = new ArrayList<>();
        ips.add(subnet.split("/")[0]);
        return ips;
    }

    private String detectOperatingSystem(String target) {
        // 简化的OS检测，实际可以通过TTL值、TCP指纹等方法
        return "Unknown";
    }

    private String guessDeviceType(AssetDiscoveryResult result) {
        // 根据开放端口推测设备类型
        try {
            List<Integer> openPorts = objectMapper.readValue(result.getOpenPorts(), List.class);
            if (openPorts.contains(80) || openPorts.contains(443)) {
                return "Web Server";
            } else if (openPorts.contains(22)) {
                return "Linux Server";
            } else if (openPorts.contains(3389)) {
                return "Windows Server";
            }
        } catch (Exception e) {
            logger.debug("推测设备类型失败: {}", e.getMessage());
        }
        return "Unknown";
    }

    private String guessVendor(AssetDiscoveryResult result) {
        // 根据服务特征推测厂商
        return "Unknown";
    }

    private BigDecimal calculateConfidenceScore(AssetDiscoveryResult result) {
        // 根据检测到的信息计算置信度
        BigDecimal score = BigDecimal.ZERO;
        if (result.getIsAlive()) score = score.add(BigDecimal.valueOf(0.3));
        if (result.getOpenPorts() != null) score = score.add(BigDecimal.valueOf(0.3));
        if (result.getOperatingSystem() != null) score = score.add(BigDecimal.valueOf(0.2));
        if (result.getServices() != null) score = score.add(BigDecimal.valueOf(0.2));
        return score;
    }

    private void mergeResults(AssetDiscoveryResult target, AssetDiscoveryResult source) {
        if (source.getOpenPorts() != null) target.setOpenPorts(source.getOpenPorts());
        if (source.getServices() != null) target.setServices(source.getServices());
        if (source.getOperatingSystem() != null) target.setOperatingSystem(source.getOperatingSystem());
        if (source.getDeviceType() != null) target.setDeviceType(source.getDeviceType());
        if (source.getVendor() != null) target.setVendor(source.getVendor());
        if (source.getConfidenceScore() != null) target.setConfidenceScore(source.getConfidenceScore());
    }
}
