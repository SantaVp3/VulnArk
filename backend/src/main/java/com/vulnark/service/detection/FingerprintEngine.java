package com.vulnark.service.detection;

import com.vulnark.entity.Asset;
import com.vulnark.entity.AssetFingerprint;
import com.vulnark.repository.AssetFingerprintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.vulnark.security.SecureHttpClientFactory;

@Service
public class FingerprintEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(FingerprintEngine.class);
    
    @Autowired
    private AssetFingerprintRepository fingerprintRepository;
    
    @Autowired
    private SecureHttpClientFactory httpClientFactory;
    
    // 指纹识别规则
    private static final Map<String, FingerprintRule> FINGERPRINT_RULES = new HashMap<>();
    
    static {
        initializeFingerprintRules();
    }
    
    /**
     * 异步执行指纹识别
     */
    @Async
    public CompletableFuture<List<AssetFingerprint>> identifyFingerprintsAsync(Asset asset) {
        logger.info("开始指纹识别: {} (ID: {})", asset.getName(), asset.getId());
        
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        try {
            // 根据资产类型选择识别方法
            switch (asset.getType()) {
                case WEB_APPLICATION:
                    fingerprints.addAll(identifyWebFingerprints(asset));
                    break;
                case SERVER:
                case WORKSTATION:
                    fingerprints.addAll(identifyServerFingerprints(asset));
                    break;
                case DATABASE:
                    fingerprints.addAll(identifyDatabaseFingerprints(asset));
                    break;
                default:
                    fingerprints.addAll(identifyGenericFingerprints(asset));
                    break;
            }
            
            // 保存识别结果
            for (AssetFingerprint fingerprint : fingerprints) {
                // 检查是否已存在相同指纹
                if (!fingerprintRepository.existsByAssetIdAndTypeAndName(
                        fingerprint.getAssetId(), fingerprint.getType(), fingerprint.getName())) {
                    fingerprintRepository.save(fingerprint);
                }
            }
            
            logger.info("指纹识别完成: {} (ID: {}), 识别到 {} 个指纹", 
                       asset.getName(), asset.getId(), fingerprints.size());
            
        } catch (Exception e) {
            logger.error("指纹识别失败: {} (ID: {})", asset.getName(), asset.getId(), e);
        }
        
        return CompletableFuture.completedFuture(fingerprints);
    }
    
    /**
     * 识别Web应用指纹
     */
    private List<AssetFingerprint> identifyWebFingerprints(Asset asset) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        String target = getTargetFromAsset(asset);
        int port = asset.getPort() != null ? asset.getPort() : 80;
        
        try {
            // HTTP指纹识别
            fingerprints.addAll(identifyHttpFingerprints(asset.getId(), target, port, false));
            
            // HTTPS指纹识别
            if (port == 443 || asset.getProtocol() != null && asset.getProtocol().toLowerCase().contains("https")) {
                fingerprints.addAll(identifyHttpFingerprints(asset.getId(), target, 443, true));
            }
            
        } catch (Exception e) {
            logger.error("Web指纹识别失败: {}", e.getMessage());
        }
        
        return fingerprints;
    }
    
    /**
     * 识别HTTP/HTTPS指纹
     */
    private List<AssetFingerprint> identifyHttpFingerprints(Long assetId, String target, int port, boolean isHttps) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        try {
            String protocol = isHttps ? "https" : "http";
            String url = String.format("%s://%s:%d", protocol, target, port);
            
            HttpURLConnection connection = createHttpConnection(url, isHttps);
            
            // 获取响应头
            Map<String, List<String>> headers = connection.getHeaderFields();
            String responseBody = readResponseBody(connection);
            
            // 基于响应头的指纹识别
            fingerprints.addAll(identifyByHeaders(assetId, headers, port));
            
            // 基于响应内容的指纹识别
            fingerprints.addAll(identifyByContent(assetId, responseBody, port));
            
            // 基于错误页面的指纹识别
            fingerprints.addAll(identifyByErrorPages(assetId, target, port, isHttps));
            
        } catch (Exception e) {
            logger.debug("HTTP指纹识别失败: {}", e.getMessage());
        }
        
        return fingerprints;
    }
    
    /**
     * 基于HTTP响应头识别指纹
     */
    private List<AssetFingerprint> identifyByHeaders(Long assetId, Map<String, List<String>> headers, int port) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            if (headerName == null) continue;
            
            String headerValue = String.join(", ", entry.getValue());
            
            // 检查每个指纹规则
            for (FingerprintRule rule : FINGERPRINT_RULES.values()) {
                if (rule.getMethod() == AssetFingerprint.IdentificationMethod.HTTP_HEADER) {
                    if (headerName.equalsIgnoreCase(rule.getHeaderName()) && 
                        rule.getPattern().matcher(headerValue).find()) {
                        
                        AssetFingerprint fingerprint = new AssetFingerprint(
                            assetId, rule.getType(), rule.getName(), rule.getConfidence());
                        fingerprint.setVersion(extractVersion(headerValue, rule.getVersionPattern()));
                        fingerprint.setVendor(rule.getVendor());
                        fingerprint.setMethod(AssetFingerprint.IdentificationMethod.HTTP_HEADER);
                        fingerprint.setSignature(headerName + ": " + headerValue);
                        fingerprint.setPort(port);
                        fingerprint.setProtocol("HTTP");
                        fingerprint.setHttpHeaders(formatHeaders(headers));
                        
                        fingerprints.add(fingerprint);
                    }
                }
            }
        }
        
        return fingerprints;
    }
    
    /**
     * 基于响应内容识别指纹
     */
    private List<AssetFingerprint> identifyByContent(Long assetId, String content, int port) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return fingerprints;
        }
        
        // 提取页面标题
        String title = extractPageTitle(content);
        
        for (FingerprintRule rule : FINGERPRINT_RULES.values()) {
            if (rule.getMethod() == AssetFingerprint.IdentificationMethod.PAGE_CONTENT) {
                Matcher matcher = rule.getPattern().matcher(content);
                if (matcher.find()) {
                    AssetFingerprint fingerprint = new AssetFingerprint(
                        assetId, rule.getType(), rule.getName(), rule.getConfidence());
                    fingerprint.setVersion(extractVersion(content, rule.getVersionPattern()));
                    fingerprint.setVendor(rule.getVendor());
                    fingerprint.setMethod(AssetFingerprint.IdentificationMethod.PAGE_CONTENT);
                    fingerprint.setSignature(matcher.group());
                    fingerprint.setPort(port);
                    fingerprint.setProtocol("HTTP");
                    fingerprint.setPageTitle(title);
                    
                    fingerprints.add(fingerprint);
                }
            }
        }
        
        return fingerprints;
    }
    
    /**
     * 基于错误页面识别指纹
     */
    private List<AssetFingerprint> identifyByErrorPages(Long assetId, String target, int port, boolean isHttps) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        // 尝试访问不存在的页面来获取错误页面
        String[] errorPaths = {"/nonexistent", "/404", "/error", "/test"};
        
        for (String path : errorPaths) {
            try {
                String protocol = isHttps ? "https" : "http";
                String url = String.format("%s://%s:%d%s", protocol, target, port, path);
                
                HttpURLConnection connection = createHttpConnection(url, isHttps);
                String errorContent = readResponseBody(connection);
                
                if (errorContent != null && !errorContent.isEmpty()) {
                    for (FingerprintRule rule : FINGERPRINT_RULES.values()) {
                        if (rule.getMethod() == AssetFingerprint.IdentificationMethod.ERROR_PAGE) {
                            Matcher matcher = rule.getPattern().matcher(errorContent);
                            if (matcher.find()) {
                                AssetFingerprint fingerprint = new AssetFingerprint(
                                    assetId, rule.getType(), rule.getName(), rule.getConfidence());
                                fingerprint.setMethod(AssetFingerprint.IdentificationMethod.ERROR_PAGE);
                                fingerprint.setSignature(matcher.group());
                                fingerprint.setPort(port);
                                fingerprint.setErrorPage(errorContent.substring(0, Math.min(1000, errorContent.length())));
                                
                                fingerprints.add(fingerprint);
                                break; // 找到一个就够了
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                logger.debug("错误页面检测失败: {}", e.getMessage());
            }
        }
        
        return fingerprints;
    }
    
    /**
     * 识别服务器指纹
     */
    private List<AssetFingerprint> identifyServerFingerprints(Asset asset) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        // 如果有Web服务，先识别Web指纹
        if (asset.getPort() != null && (asset.getPort() == 80 || asset.getPort() == 443 || 
            asset.getPort() == 8080 || asset.getPort() == 8443)) {
            fingerprints.addAll(identifyWebFingerprints(asset));
        }
        
        // TODO: 添加其他服务器指纹识别逻辑
        
        return fingerprints;
    }
    
    /**
     * 识别数据库指纹
     */
    private List<AssetFingerprint> identifyDatabaseFingerprints(Asset asset) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        // 根据端口推断数据库类型
        if (asset.getPort() != null) {
            String dbType = getDatabaseTypeByPort(asset.getPort());
            if (!"Unknown Database".equals(dbType)) {
                AssetFingerprint fingerprint = new AssetFingerprint(
                    asset.getId(), AssetFingerprint.FingerprintType.DATABASE, dbType, 70);
                fingerprint.setMethod(AssetFingerprint.IdentificationMethod.PORT_SERVICE);
                fingerprint.setPort(asset.getPort());
                fingerprint.setSignature("Port-based identification");
                
                fingerprints.add(fingerprint);
            }
        }
        
        return fingerprints;
    }
    
    /**
     * 通用指纹识别
     */
    private List<AssetFingerprint> identifyGenericFingerprints(Asset asset) {
        List<AssetFingerprint> fingerprints = new ArrayList<>();
        
        // 尝试Web指纹识别
        if (asset.getPort() != null && isWebPort(asset.getPort())) {
            fingerprints.addAll(identifyWebFingerprints(asset));
        }
        
        return fingerprints;
    }
    
    // 工具方法
    private String getTargetFromAsset(Asset asset) {
        if (asset.getIpAddress() != null && !asset.getIpAddress().isEmpty()) {
            return asset.getIpAddress();
        } else if (asset.getDomain() != null && !asset.getDomain().isEmpty()) {
            return asset.getDomain();
        } else {
            return asset.getName();
        }
    }
    
    private boolean isWebPort(int port) {
        int[] webPorts = {80, 443, 8080, 8443, 8000, 8888, 9000, 3000};
        for (int webPort : webPorts) {
            if (port == webPort) return true;
        }
        return false;
    }
    
    private String getDatabaseTypeByPort(int port) {
        switch (port) {
            case 1433: return "Microsoft SQL Server";
            case 1521: return "Oracle Database";
            case 3306: return "MySQL";
            case 5432: return "PostgreSQL";
            case 6379: return "Redis";
            case 27017: return "MongoDB";
            default: return "Unknown Database";
        }
    }

    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createHttpConnection(String url, boolean isHttps) throws Exception {
        // 使用安全的HTTP客户端工厂创建连接
        HttpURLConnection connection = httpClientFactory.createSecureConnection(url, "VulnArk-Scanner/1.0");
        
        // 设置指纹识别特定的属性
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setInstanceFollowRedirects(false); // 不自动跟随重定向

        return connection;
    }

    /**
     * 读取响应内容
     */
    private String readResponseBody(HttpURLConnection connection) {
        try {
            BufferedReader reader;
            if (connection.getResponseCode() >= 400) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }

            StringBuilder content = new StringBuilder();
            String line;
            int maxLines = 100; // 限制读取行数
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                content.append(line).append("\n");
                lineCount++;
            }

            reader.close();
            return content.toString();

        } catch (Exception e) {
            logger.debug("读取响应内容失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 提取页面标题
     */
    private String extractPageTitle(String content) {
        Pattern titlePattern = Pattern.compile("<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = titlePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 提取版本信息
     */
    private String extractVersion(String content, Pattern versionPattern) {
        if (versionPattern == null) return null;

        Matcher matcher = versionPattern.matcher(content);
        if (matcher.find()) {
            return matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
        }
        return null;
    }

    /**
     * 格式化HTTP响应头
     */
    private String formatHeaders(Map<String, List<String>> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null) {
                sb.append(entry.getKey()).append(": ")
                  .append(String.join(", ", entry.getValue())).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 初始化指纹识别规则
     */
    private static void initializeFingerprintRules() {
        // Web服务器指纹规则
        addRule("apache", AssetFingerprint.FingerprintType.WEB_SERVER, "Apache HTTP Server",
                AssetFingerprint.IdentificationMethod.HTTP_HEADER, "Server",
                Pattern.compile("Apache", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Apache/([\\d.]+)"), "Apache Software Foundation", 90);

        addRule("nginx", AssetFingerprint.FingerprintType.WEB_SERVER, "Nginx",
                AssetFingerprint.IdentificationMethod.HTTP_HEADER, "Server",
                Pattern.compile("nginx", Pattern.CASE_INSENSITIVE),
                Pattern.compile("nginx/([\\d.]+)"), "Nginx Inc.", 90);

        addRule("iis", AssetFingerprint.FingerprintType.WEB_SERVER, "Microsoft IIS",
                AssetFingerprint.IdentificationMethod.HTTP_HEADER, "Server",
                Pattern.compile("Microsoft-IIS", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Microsoft-IIS/([\\d.]+)"), "Microsoft", 90);

        // 应用服务器指纹规则
        addRule("tomcat", AssetFingerprint.FingerprintType.APPLICATION_SERVER, "Apache Tomcat",
                AssetFingerprint.IdentificationMethod.HTTP_HEADER, "Server",
                Pattern.compile("Tomcat", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Tomcat/([\\d.]+)"), "Apache Software Foundation", 85);

        addRule("jetty", AssetFingerprint.FingerprintType.APPLICATION_SERVER, "Eclipse Jetty",
                AssetFingerprint.IdentificationMethod.HTTP_HEADER, "Server",
                Pattern.compile("Jetty", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Jetty\\(([\\d.]+)\\)"), "Eclipse Foundation", 85);

        // 编程语言/框架指纹规则
        addRule("php", AssetFingerprint.FingerprintType.PROGRAMMING_LANGUAGE, "PHP",
                AssetFingerprint.IdentificationMethod.HTTP_HEADER, "X-Powered-By",
                Pattern.compile("PHP", Pattern.CASE_INSENSITIVE),
                Pattern.compile("PHP/([\\d.]+)"), "PHP Group", 80);

        addRule("aspnet", AssetFingerprint.FingerprintType.WEB_FRAMEWORK, "ASP.NET",
                AssetFingerprint.IdentificationMethod.HTTP_HEADER, "X-AspNet-Version",
                Pattern.compile(".*"),
                Pattern.compile("([\\d.]+)"), "Microsoft", 85);

        // CMS指纹规则
        addRule("wordpress", AssetFingerprint.FingerprintType.CMS, "WordPress",
                AssetFingerprint.IdentificationMethod.PAGE_CONTENT, null,
                Pattern.compile("wp-content|wordpress", Pattern.CASE_INSENSITIVE),
                Pattern.compile("WordPress ([\\d.]+)"), "WordPress.org", 75);

        addRule("drupal", AssetFingerprint.FingerprintType.CMS, "Drupal",
                AssetFingerprint.IdentificationMethod.PAGE_CONTENT, null,
                Pattern.compile("Drupal", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Drupal ([\\d.]+)"), "Drupal Association", 75);

        // 错误页面指纹规则
        addRule("tomcat_error", AssetFingerprint.FingerprintType.APPLICATION_SERVER, "Apache Tomcat",
                AssetFingerprint.IdentificationMethod.ERROR_PAGE, null,
                Pattern.compile("Apache Tomcat", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Apache Tomcat/([\\d.]+)"), "Apache Software Foundation", 80);

        addRule("iis_error", AssetFingerprint.FingerprintType.WEB_SERVER, "Microsoft IIS",
                AssetFingerprint.IdentificationMethod.ERROR_PAGE, null,
                Pattern.compile("Internet Information Services", Pattern.CASE_INSENSITIVE),
                null, "Microsoft", 75);
    }

    /**
     * 添加指纹规则
     */
    private static void addRule(String id, AssetFingerprint.FingerprintType type, String name,
                               AssetFingerprint.IdentificationMethod method, String headerName,
                               Pattern pattern, Pattern versionPattern, String vendor, int confidence) {
        FingerprintRule rule = new FingerprintRule();
        rule.setId(id);
        rule.setType(type);
        rule.setName(name);
        rule.setMethod(method);
        rule.setHeaderName(headerName);
        rule.setPattern(pattern);
        rule.setVersionPattern(versionPattern);
        rule.setVendor(vendor);
        rule.setConfidence(confidence);

        FINGERPRINT_RULES.put(id, rule);
    }

    /**
     * 指纹规则内部类
     */
    private static class FingerprintRule {
        private String id;
        private AssetFingerprint.FingerprintType type;
        private String name;
        private AssetFingerprint.IdentificationMethod method;
        private String headerName;
        private Pattern pattern;
        private Pattern versionPattern;
        private String vendor;
        private int confidence;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public AssetFingerprint.FingerprintType getType() { return type; }
        public void setType(AssetFingerprint.FingerprintType type) { this.type = type; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public AssetFingerprint.IdentificationMethod getMethod() { return method; }
        public void setMethod(AssetFingerprint.IdentificationMethod method) { this.method = method; }

        public String getHeaderName() { return headerName; }
        public void setHeaderName(String headerName) { this.headerName = headerName; }

        public Pattern getPattern() { return pattern; }
        public void setPattern(Pattern pattern) { this.pattern = pattern; }

        public Pattern getVersionPattern() { return versionPattern; }
        public void setVersionPattern(Pattern versionPattern) { this.versionPattern = versionPattern; }

        public String getVendor() { return vendor; }
        public void setVendor(String vendor) { this.vendor = vendor; }

        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }
    }
}
