package com.vulnark.config;

import com.vulnark.service.ScanToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 扫描工具初始化器
 * 在应用启动时自动初始化扫描工具
 */
@Component
public class ScanToolInitializer implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanToolInitializer.class);
    
    @Autowired
    private ScanToolService scanToolService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始初始化扫描工具...");
        
        try {
            // 初始化默认工具配置
            scanToolService.initializeDefaultTools();
            
            // 检查工具更新
            scanToolService.checkUpdates();
            
            logger.info("扫描工具初始化完成");
        } catch (Exception e) {
            logger.error("扫描工具初始化失败: {}", e.getMessage(), e);
        }
    }
}
