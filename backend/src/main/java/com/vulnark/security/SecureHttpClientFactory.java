package com.vulnark.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 安全HTTP客户端工厂
 * 提供可配置的SSL验证机制
 */
@Component
public class SecureHttpClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(SecureHttpClientFactory.class);

    @Value("${vulnark.scan.ssl.verification.enabled:true}")
    private boolean sslVerificationEnabled;

    @Value("${vulnark.scan.ssl.verification.allow-self-signed:false}")
    private boolean allowSelfSignedCertificates;

    @Value("${vulnark.scan.connection.timeout:30000}")
    private int connectionTimeout;

    @Value("${vulnark.scan.read.timeout:30000}")
    private int readTimeout;

    /**
     * 创建安全的HTTP连接
     *
     * @param url 目标URL
     * @param userAgent 用户代理
     * @return HTTP连接
     */
    public HttpURLConnection createSecureConnection(String url, String userAgent) throws Exception {
        return createSecureConnection(url, userAgent, "GET");
    }

    /**
     * 创建安全的HTTP连接
     *
     * @param url 目标URL
     * @param userAgent 用户代理  
     * @param method HTTP方法
     * @return HTTP连接
     */
    public HttpURLConnection createSecureConnection(String url, String userAgent, String method) throws Exception {
        URL targetUrl = new URL(url);
        HttpURLConnection connection;

        if (url.toLowerCase().startsWith("https://")) {
            connection = createHttpsConnection(targetUrl, userAgent, method);
        } else {
            connection = createHttpConnection(targetUrl, userAgent, method);
        }

        // 设置通用属性
        connection.setRequestMethod(method);
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setInstanceFollowRedirects(false);

        return connection;
    }

    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createHttpConnection(URL url, String userAgent, String method) throws Exception {
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * 创建HTTPS连接
     */
    private HttpsURLConnection createHttpsConnection(URL url, String userAgent, String method) throws Exception {
        HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();

        if (!sslVerificationEnabled) {
            // 仅在明确配置且必要的情况下才禁用SSL验证（如内部测试环境）
            logger.warn("SSL证书验证已禁用 - 仅应在受控的测试环境中使用！");
            configureTrustAllCertificates(httpsConnection);
        } else if (allowSelfSignedCertificates) {
            // 允许自签名证书但仍进行基本验证
            logger.info("允许自签名证书");
            configurePermissiveCertificateValidation(httpsConnection);
        } else {
            // 使用默认的严格SSL验证
            logger.debug("使用严格的SSL证书验证");
        }

        return httpsConnection;
    }

    /**
     * 配置信任所有证书（仅用于特殊测试场景）
     */
    private void configureTrustAllCertificates(HttpsURLConnection connection) 
            throws NoSuchAlgorithmException, KeyManagementException {
        
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) 
                        throws CertificateException {
                    // 记录警告但允许通过
                    logger.warn("接受客户端证书（SSL验证已禁用）");
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) 
                        throws CertificateException {
                    // 记录警告但允许通过
                    logger.warn("接受服务器证书（SSL验证已禁用）");
                }
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        connection.setHostnameVerifier((hostname, session) -> {
            logger.warn("跳过主机名验证: {}", hostname);
            return true;
        });
    }

    /**
     * 配置宽松的证书验证（允许自签名证书）
     */
    private void configurePermissiveCertificateValidation(HttpsURLConnection connection) 
            throws NoSuchAlgorithmException, KeyManagementException {
        
        TrustManager[] permissiveTrustManager = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) 
                        throws CertificateException {
                    // 对客户端证书进行基本检查
                    if (certs == null || certs.length == 0) {
                        throw new CertificateException("客户端证书为空");
                    }
                    logger.debug("接受客户端证书: {}", certs[0].getSubjectDN());
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) 
                        throws CertificateException {
                    // 对服务器证书进行基本检查
                    if (certs == null || certs.length == 0) {
                        throw new CertificateException("服务器证书为空");
                    }
                    
                    // 检查证书是否过期
                    X509Certificate cert = certs[0];
                    try {
                        cert.checkValidity();
                    } catch (CertificateException e) {
                        logger.warn("证书有效性检查失败: {}", e.getMessage());
                        // 在允许自签名证书模式下，记录警告但不阻止连接
                    }
                    
                    logger.debug("接受服务器证书: {}", cert.getSubjectDN());
                }
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, permissiveTrustManager, new SecureRandom());
        
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        
        // 宽松的主机名验证
        connection.setHostnameVerifier((hostname, session) -> {
            logger.debug("验证主机名: {}", hostname);
            // 在测试环境中可能需要较宽松的主机名验证
            return true;
        });
    }

    /**
     * 获取SSL证书信息
     */
    public String getCertificateInfo(HttpsURLConnection connection) {
        try {
            connection.connect();
            java.security.cert.Certificate[] certs = connection.getServerCertificates();
            
            if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate) certs[0];
                return String.format(
                    "证书主题: %s, 颁发者: %s, 有效期: %s 至 %s",
                    cert.getSubjectDN().getName(),
                    cert.getIssuerDN().getName(),
                    cert.getNotBefore(),
                    cert.getNotAfter()
                );
            }
        } catch (Exception e) {
            logger.debug("获取证书信息失败: {}", e.getMessage());
        }
        
        return "无法获取证书信息";
    }

    /**
     * 检查是否启用SSL验证
     */
    public boolean isSslVerificationEnabled() {
        return sslVerificationEnabled;
    }

    /**
     * 检查是否允许自签名证书
     */
    public boolean isAllowSelfSignedCertificates() {
        return allowSelfSignedCertificates;
    }
} 