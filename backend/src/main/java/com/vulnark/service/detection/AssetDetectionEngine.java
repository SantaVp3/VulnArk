package com.vulnark.service.detection;

import com.vulnark.entity.Asset;
import com.vulnark.entity.AssetDetection;
import com.vulnark.repository.AssetDetectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;
import javax.security.cert.CertificateException;

@Service
public class AssetDetectionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AssetDetectionEngine.class);
    
    @Autowired
    private AssetDetectionRepository detectionRepository;
    
    // 默认超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 5000;
    private static final int PING_TIMEOUT = 3000;
    private static final int TCP_TIMEOUT = 5000;
    
    // 常用端口列表
    private static final int[] COMMON_PORTS = {
        21, 22, 23, 25, 53, 80, 110, 143, 443, 993, 995,
        1433, 1521, 3306, 3389, 5432, 5984, 6379, 8080, 8443, 9200
    };
    
    /**
     * 异步检测资产状态
     */
    @Async
    public CompletableFuture<List<AssetDetection>> detectAssetAsync(Asset asset) {
        logger.info("开始异步检测资产: {} (ID: {})", asset.getName(), asset.getId());
        
        List<AssetDetection> detections = new ArrayList<>();
        
        try {
            // 根据资产类型选择检测方法
            switch (asset.getType()) {
                case SERVER:
                case WORKSTATION:
                    detections.addAll(detectHostAsset(asset));
                    break;
                case WEB_APPLICATION:
                    detections.addAll(detectWebAsset(asset));
                    break;
                case DATABASE:
                    detections.addAll(detectDatabaseAsset(asset));
                    break;
                case NETWORK_DEVICE:
                    detections.addAll(detectNetworkDevice(asset));
                    break;
                default:
                    detections.addAll(detectGenericAsset(asset));
                    break;
            }
            
            logger.info("资产检测完成: {} (ID: {}), 检测记录数: {}", 
                       asset.getName(), asset.getId(), detections.size());
            
        } catch (Exception e) {
            logger.error("资产检测失败: {} (ID: {})", asset.getName(), asset.getId(), e);
            
            // 创建失败记录
            AssetDetection failedDetection = new AssetDetection(
                asset.getId(), 
                AssetDetection.DetectionType.PING, 
                getTargetFromAsset(asset)
            );
            failedDetection.markAsFailed("检测过程中发生异常: " + e.getMessage());
            detections.add(detectionRepository.save(failedDetection));
        }
        
        return CompletableFuture.completedFuture(detections);
    }
    
    /**
     * 检测主机类资产
     */
    private List<AssetDetection> detectHostAsset(Asset asset) {
        List<AssetDetection> detections = new ArrayList<>();
        String target = getTargetFromAsset(asset);
        
        // PING检测
        AssetDetection pingDetection = performPingDetection(asset.getId(), target);
        detections.add(pingDetection);
        
        // 如果PING成功，进行端口扫描
        if (pingDetection.isOnline()) {
            // 检测常用端口
            for (int port : COMMON_PORTS) {
                AssetDetection portDetection = performTcpPortDetection(asset.getId(), target, port);
                detections.add(portDetection);
            }
            
            // 如果资产指定了端口，也要检测
            if (asset.getPort() != null && asset.getPort() > 0) {
                AssetDetection customPortDetection = performTcpPortDetection(
                    asset.getId(), target, asset.getPort());
                detections.add(customPortDetection);
            }
        }
        
        return detections;
    }
    
    /**
     * 检测Web应用资产
     */
    private List<AssetDetection> detectWebAsset(Asset asset) {
        List<AssetDetection> detections = new ArrayList<>();
        String target = getTargetFromAsset(asset);
        
        // HTTP检测
        AssetDetection httpDetection = performHttpDetection(asset.getId(), target, 80);
        detections.add(httpDetection);
        
        // HTTPS检测
        AssetDetection httpsDetection = performHttpsDetection(asset.getId(), target, 443);
        detections.add(httpsDetection);
        
        // 如果指定了自定义端口
        if (asset.getPort() != null && asset.getPort() > 0 && 
            asset.getPort() != 80 && asset.getPort() != 443) {
            AssetDetection customDetection = performHttpDetection(
                asset.getId(), target, asset.getPort());
            detections.add(customDetection);
        }
        
        return detections;
    }
    
    /**
     * 检测数据库资产
     */
    private List<AssetDetection> detectDatabaseAsset(Asset asset) {
        List<AssetDetection> detections = new ArrayList<>();
        String target = getTargetFromAsset(asset);
        
        // 常见数据库端口
        int[] dbPorts = {1433, 1521, 3306, 5432, 6379, 27017};
        
        for (int port : dbPorts) {
            AssetDetection dbDetection = performDatabaseDetection(asset.getId(), target, port);
            detections.add(dbDetection);
        }
        
        // 自定义端口
        if (asset.getPort() != null && asset.getPort() > 0) {
            AssetDetection customDetection = performDatabaseDetection(
                asset.getId(), target, asset.getPort());
            detections.add(customDetection);
        }
        
        return detections;
    }
    
    /**
     * 检测网络设备
     */
    private List<AssetDetection> detectNetworkDevice(Asset asset) {
        List<AssetDetection> detections = new ArrayList<>();
        String target = getTargetFromAsset(asset);
        
        // PING检测
        AssetDetection pingDetection = performPingDetection(asset.getId(), target);
        detections.add(pingDetection);
        
        // SNMP检测
        AssetDetection snmpDetection = performTcpPortDetection(asset.getId(), target, 161);
        detections.add(snmpDetection);
        
        // SSH检测
        AssetDetection sshDetection = performSshDetection(asset.getId(), target, 22);
        detections.add(sshDetection);
        
        // Telnet检测
        AssetDetection telnetDetection = performTcpPortDetection(asset.getId(), target, 23);
        detections.add(telnetDetection);
        
        return detections;
    }
    
    /**
     * 通用资产检测
     */
    private List<AssetDetection> detectGenericAsset(Asset asset) {
        List<AssetDetection> detections = new ArrayList<>();
        String target = getTargetFromAsset(asset);
        
        // PING检测
        AssetDetection pingDetection = performPingDetection(asset.getId(), target);
        detections.add(pingDetection);
        
        // 如果指定了端口，检测该端口
        if (asset.getPort() != null && asset.getPort() > 0) {
            AssetDetection portDetection = performTcpPortDetection(
                asset.getId(), target, asset.getPort());
            detections.add(portDetection);
        }
        
        return detections;
    }
    
    /**
     * 执行PING检测
     */
    private AssetDetection performPingDetection(Long assetId, String target) {
        AssetDetection detection = new AssetDetection(assetId, AssetDetection.DetectionType.PING, target);
        detection.markAsStarted();
        detection = detectionRepository.save(detection);
        
        try {
            InetAddress address = InetAddress.getByName(target);
            long startTime = System.currentTimeMillis();
            
            boolean reachable = address.isReachable(PING_TIMEOUT);
            long responseTime = System.currentTimeMillis() - startTime;
            
            detection.setResponseTime(responseTime);
            
            if (reachable) {
                detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);
                detection.setDetails("PING成功，响应时间: " + responseTime + "ms");
            } else {
                detection.markAsCompleted(AssetDetection.DetectionResult.OFFLINE);
                detection.setDetails("PING失败，主机不可达");
            }
            
        } catch (UnknownHostException e) {
            detection.markAsFailed("无法解析主机名: " + target);
        } catch (IOException e) {
            detection.markAsFailed("PING检测失败: " + e.getMessage());
        } catch (Exception e) {
            detection.markAsFailed("检测过程中发生异常: " + e.getMessage());
        }
        
        return detectionRepository.save(detection);
    }
    
    /**
     * 执行TCP端口检测
     */
    private AssetDetection performTcpPortDetection(Long assetId, String target, int port) {
        AssetDetection detection = new AssetDetection(
            assetId, AssetDetection.DetectionType.TCP_PORT, target, port);
        detection.markAsStarted();
        detection = detectionRepository.save(detection);
        
        try {
            long startTime = System.currentTimeMillis();
            
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(target, port), TCP_TIMEOUT);
                long responseTime = System.currentTimeMillis() - startTime;
                
                detection.setResponseTime(responseTime);
                detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);
                detection.setDetails(String.format("端口 %d 开放，响应时间: %dms", port, responseTime));
                
            } catch (SocketTimeoutException e) {
                detection.markAsTimeout();
                detection.setDetails(String.format("端口 %d 连接超时", port));
            } catch (ConnectException e) {
                detection.markAsCompleted(AssetDetection.DetectionResult.OFFLINE);
                detection.setDetails(String.format("端口 %d 关闭或被过滤", port));
            }
            
        } catch (Exception e) {
            detection.markAsFailed("TCP端口检测失败: " + e.getMessage());
        }
        
        return detectionRepository.save(detection);
    }
    
    /**
     * 从资产对象获取目标地址
     */
    private String getTargetFromAsset(Asset asset) {
        if (asset.getIpAddress() != null && !asset.getIpAddress().isEmpty()) {
            return asset.getIpAddress();
        } else if (asset.getDomain() != null && !asset.getDomain().isEmpty()) {
            return asset.getDomain();
        } else {
            return asset.getName(); // 最后尝试使用名称
        }
    }
    
    /**
     * 执行HTTP检测
     */
    private AssetDetection performHttpDetection(Long assetId, String target, int port) {
        AssetDetection detection = new AssetDetection(
            assetId, AssetDetection.DetectionType.HTTP_SERVICE, target, port);
        detection.markAsStarted();
        detection = detectionRepository.save(detection);

        try {
            String url = String.format("http://%s:%d", target, port);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TCP_TIMEOUT);
            connection.setReadTimeout(TCP_TIMEOUT);
            connection.setRequestProperty("User-Agent", "VulnArk-Scanner/1.0");

            long startTime = System.currentTimeMillis();
            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            detection.setResponseTime(responseTime);
            detection.setHttpStatusCode(responseCode);

            // 获取响应头信息
            StringBuilder headers = new StringBuilder();
            connection.getHeaderFields().forEach((key, values) -> {
                if (key != null) {
                    headers.append(key).append(": ").append(String.join(", ", values)).append("\n");
                }
            });
            detection.setBanner(headers.toString());

            if (responseCode >= 200 && responseCode < 400) {
                detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);
                detection.setDetails(String.format("HTTP服务正常，状态码: %d，响应时间: %dms",
                                                  responseCode, responseTime));
            } else {
                detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);
                detection.setDetails(String.format("HTTP服务响应异常，状态码: %d", responseCode));
            }

        } catch (SocketTimeoutException e) {
            detection.markAsTimeout();
            detection.setDetails("HTTP连接超时");
        } catch (ConnectException e) {
            detection.markAsCompleted(AssetDetection.DetectionResult.OFFLINE);
            detection.setDetails("HTTP服务不可用");
        } catch (Exception e) {
            detection.markAsFailed("HTTP检测失败: " + e.getMessage());
        }

        return detectionRepository.save(detection);
    }

    /**
     * 执行HTTPS检测
     */
    private AssetDetection performHttpsDetection(Long assetId, String target, int port) {
        AssetDetection detection = new AssetDetection(
            assetId, AssetDetection.DetectionType.HTTPS_SERVICE, target, port);
        detection.markAsStarted();
        detection = detectionRepository.save(detection);

        try {
            String url = String.format("https://%s:%d", target, port);

            // 创建信任所有证书的SSL上下文
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TCP_TIMEOUT);
            connection.setReadTimeout(TCP_TIMEOUT);
            connection.setRequestProperty("User-Agent", "VulnArk-Scanner/1.0");

            long startTime = System.currentTimeMillis();
            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            detection.setResponseTime(responseTime);
            detection.setHttpStatusCode(responseCode);

            // 获取SSL证书信息
            try {
                Certificate[] certs = connection.getServerCertificates();
                if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                    X509Certificate cert = (X509Certificate) certs[0];
                    detection.setDetails(String.format("HTTPS服务正常，证书主题: %s",
                                                      cert.getSubjectDN().getName()));
                }
            } catch (Exception e) {
                logger.debug("获取SSL证书信息失败", e);
            }

            detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);

        } catch (Exception e) {
            detection.markAsFailed("HTTPS检测失败: " + e.getMessage());
        }

        return detectionRepository.save(detection);
    }

    /**
     * 执行数据库检测
     */
    private AssetDetection performDatabaseDetection(Long assetId, String target, int port) {
        AssetDetection detection = new AssetDetection(
            assetId, AssetDetection.DetectionType.DATABASE_SERVICE, target, port);
        detection.markAsStarted();
        detection = detectionRepository.save(detection);

        // 先进行TCP端口检测
        try (Socket socket = new Socket()) {
            long startTime = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(target, port), TCP_TIMEOUT);
            long responseTime = System.currentTimeMillis() - startTime;

            detection.setResponseTime(responseTime);
            detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);

            // 根据端口推断数据库类型
            String dbType = getDatabaseTypeByPort(port);
            detection.setDetails(String.format("数据库服务端口开放 (%s)，响应时间: %dms",
                                              dbType, responseTime));

        } catch (Exception e) {
            detection.markAsFailed("数据库检测失败: " + e.getMessage());
        }

        return detectionRepository.save(detection);
    }

    /**
     * 执行SSH检测
     */
    private AssetDetection performSshDetection(Long assetId, String target, int port) {
        AssetDetection detection = new AssetDetection(
            assetId, AssetDetection.DetectionType.SSH_SERVICE, target, port);
        detection.markAsStarted();
        detection = detectionRepository.save(detection);

        try (Socket socket = new Socket()) {
            long startTime = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(target, port), TCP_TIMEOUT);

            // 尝试读取SSH横幅
            socket.setSoTimeout(3000);
            byte[] buffer = new byte[1024];
            int bytesRead = socket.getInputStream().read(buffer);

            long responseTime = System.currentTimeMillis() - startTime;
            detection.setResponseTime(responseTime);

            if (bytesRead > 0) {
                String banner = new String(buffer, 0, bytesRead).trim();
                detection.setBanner(banner);
                detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);
                detection.setDetails(String.format("SSH服务正常，横幅: %s", banner));
            } else {
                detection.markAsCompleted(AssetDetection.DetectionResult.ONLINE);
                detection.setDetails("SSH端口开放但无横幅信息");
            }

        } catch (Exception e) {
            detection.markAsFailed("SSH检测失败: " + e.getMessage());
        }

        return detectionRepository.save(detection);
    }

    /**
     * 根据端口推断数据库类型
     */
    private String getDatabaseTypeByPort(int port) {
        switch (port) {
            case 1433: return "SQL Server";
            case 1521: return "Oracle";
            case 3306: return "MySQL";
            case 5432: return "PostgreSQL";
            case 6379: return "Redis";
            case 27017: return "MongoDB";
            default: return "Unknown Database";
        }
    }
}
