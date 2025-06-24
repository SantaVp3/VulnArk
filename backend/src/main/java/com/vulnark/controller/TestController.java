package com.vulnark.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "测试接口", description = "用于测试系统是否正常运行")
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private DataSource dataSource;
    
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        data.put("message", "VulnArk Backend is running");
        return data;
    }

    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "VulnArk");
        data.put("version", "1.0.0");
        data.put("description", "企业级漏洞管理平台");
        data.put("author", "VulnArk Team");
        return data;
    }

    @Operation(summary = "数据库连接测试")
    @GetMapping("/db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> data = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            data.put("connected", true);
            data.put("url", connection.getMetaData().getURL());
            data.put("username", connection.getMetaData().getUserName());
            data.put("database", connection.getCatalog());
            data.put("message", "数据库连接成功");
        } catch (Exception e) {
            data.put("connected", false);
            data.put("error", e.getMessage());
            data.put("message", "数据库连接失败");
        }
        return data;
    }
}
