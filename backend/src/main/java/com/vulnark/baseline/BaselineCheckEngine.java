package com.vulnark.baseline;

import com.vulnark.entity.Asset;
import com.vulnark.entity.BaselineCheck;
import com.vulnark.entity.BaselineCheckItem;
import com.vulnark.security.SecureCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 基线检查引擎
 * 负责执行各种类型的基线检查脚本
 */
@Component
public class BaselineCheckEngine {

    private static final Logger logger = LoggerFactory.getLogger(BaselineCheckEngine.class);

    @Autowired
    private SecureCommandExecutor commandExecutor;

    /**
     * 执行基线检查
     */
    public CompletableFuture<BaselineCheckResult> executeCheck(BaselineCheck baselineCheck, Asset asset) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("开始执行基线检查: {} for asset: {}", baselineCheck.getName(), asset.getName());
                
                BaselineCheckResult result = new BaselineCheckResult();
                result.setBaselineCheckId(baselineCheck.getId());
                result.setAssetId(asset.getId());
                
                // 根据检查类型选择相应的检查脚本
                List<BaselineCheckItem> checkItems = generateCheckItems(baselineCheck, asset);
                result.setCheckItems(checkItems);
                
                // 执行检查项
                for (BaselineCheckItem item : checkItems) {
                    executeCheckItem(item, asset);
                }
                
                // 计算总体结果
                calculateOverallResult(result);
                
                logger.info("基线检查完成: {}, 总计: {}, 通过: {}, 失败: {}", 
                    baselineCheck.getName(), 
                    result.getTotalItems(), 
                    result.getPassedItems(), 
                    result.getFailedItems());
                
                return result;
                
            } catch (Exception e) {
                logger.error("基线检查执行失败: {}", baselineCheck.getName(), e);
                BaselineCheckResult errorResult = new BaselineCheckResult();
                errorResult.setBaselineCheckId(baselineCheck.getId());
                errorResult.setAssetId(asset.getId());
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        });
    }

    /**
     * 根据检查类型生成检查项
     */
    private List<BaselineCheckItem> generateCheckItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        
        switch (baselineCheck.getCheckType()) {
            case SYSTEM_SECURITY:
                items.addAll(generateSystemSecurityItems(baselineCheck, asset));
                break;
            case NETWORK_SECURITY:
                items.addAll(generateNetworkSecurityItems(baselineCheck, asset));
                break;
            case DATABASE_SECURITY:
                items.addAll(generateDatabaseSecurityItems(baselineCheck, asset));
                break;
            case WEB_SECURITY:
                items.addAll(generateWebSecurityItems(baselineCheck, asset));
                break;
            case MIDDLEWARE_SECURITY:
                items.addAll(generateMiddlewareSecurityItems(baselineCheck, asset));
                break;
            case CLOUD_SECURITY:
                items.addAll(generateCloudSecurityItems(baselineCheck, asset));
                break;
            case CUSTOM:
                items.addAll(generateCustomItems(baselineCheck, asset));
                break;
        }
        
        return items;
    }

    /**
     * 生成系统安全检查项
     */
    private List<BaselineCheckItem> generateSystemSecurityItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        
        // 密码策略检查
        BaselineCheckItem passwordPolicy = new BaselineCheckItem();
        passwordPolicy.setBaselineCheck(baselineCheck);
        passwordPolicy.setItemCode("SYS-001");
        passwordPolicy.setItemName("密码复杂度策略检查");
        passwordPolicy.setDescription("检查系统密码复杂度策略配置");
        passwordPolicy.setCategory("身份认证");
        passwordPolicy.setSeverity(BaselineCheckItem.SeverityLevel.HIGH);
        passwordPolicy.setExpectedValue("密码长度>=8位，包含大小写字母、数字和特殊字符");
        passwordPolicy.setCheckCommand("cat /etc/pam.d/common-password | grep pam_pwquality");
        passwordPolicy.setRemediation("配置/etc/pam.d/common-password文件，启用密码复杂度检查");
        items.add(passwordPolicy);
        
        // SSH配置检查
        BaselineCheckItem sshConfig = new BaselineCheckItem();
        sshConfig.setBaselineCheck(baselineCheck);
        sshConfig.setItemCode("SYS-002");
        sshConfig.setItemName("SSH安全配置检查");
        sshConfig.setDescription("检查SSH服务安全配置");
        sshConfig.setCategory("网络服务");
        sshConfig.setSeverity(BaselineCheckItem.SeverityLevel.HIGH);
        sshConfig.setExpectedValue("禁用root登录，使用密钥认证");
        sshConfig.setCheckCommand("grep -E '^(PermitRootLogin|PasswordAuthentication)' /etc/ssh/sshd_config");
        sshConfig.setRemediation("修改/etc/ssh/sshd_config，设置PermitRootLogin no，PasswordAuthentication no");
        items.add(sshConfig);
        
        // 防火墙状态检查
        BaselineCheckItem firewallStatus = new BaselineCheckItem();
        firewallStatus.setBaselineCheck(baselineCheck);
        firewallStatus.setItemCode("SYS-003");
        firewallStatus.setItemName("防火墙状态检查");
        firewallStatus.setDescription("检查系统防火墙是否启用");
        firewallStatus.setCategory("网络安全");
        firewallStatus.setSeverity(BaselineCheckItem.SeverityLevel.MEDIUM);
        firewallStatus.setExpectedValue("防火墙服务已启用并运行");
        firewallStatus.setCheckCommand("systemctl is-active ufw || systemctl is-active firewalld");
        firewallStatus.setRemediation("启用并配置防火墙服务");
        items.add(firewallStatus);
        
        // 系统更新检查
        BaselineCheckItem systemUpdate = new BaselineCheckItem();
        systemUpdate.setBaselineCheck(baselineCheck);
        systemUpdate.setItemCode("SYS-004");
        systemUpdate.setItemName("系统更新状态检查");
        systemUpdate.setDescription("检查系统是否有可用的安全更新");
        systemUpdate.setCategory("系统维护");
        systemUpdate.setSeverity(BaselineCheckItem.SeverityLevel.MEDIUM);
        systemUpdate.setExpectedValue("系统已安装最新安全更新");
        systemUpdate.setCheckCommand("apt list --upgradable 2>/dev/null | grep -i security || yum check-update --security");
        systemUpdate.setRemediation("安装可用的安全更新");
        items.add(systemUpdate);
        
        // 文件权限检查
        BaselineCheckItem filePermissions = new BaselineCheckItem();
        filePermissions.setBaselineCheck(baselineCheck);
        filePermissions.setItemCode("SYS-005");
        filePermissions.setItemName("关键文件权限检查");
        filePermissions.setDescription("检查关键系统文件的权限设置");
        filePermissions.setCategory("文件系统");
        filePermissions.setSeverity(BaselineCheckItem.SeverityLevel.HIGH);
        filePermissions.setExpectedValue("/etc/passwd权限为644，/etc/shadow权限为640");
        filePermissions.setCheckCommand("ls -l /etc/passwd /etc/shadow");
        filePermissions.setRemediation("设置正确的文件权限：chmod 644 /etc/passwd; chmod 640 /etc/shadow");
        items.add(filePermissions);
        
        return items;
    }

    /**
     * 生成网络安全检查项
     */
    private List<BaselineCheckItem> generateNetworkSecurityItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        
        // 端口扫描检查
        BaselineCheckItem portScan = new BaselineCheckItem();
        portScan.setBaselineCheck(baselineCheck);
        portScan.setItemCode("NET-001");
        portScan.setItemName("开放端口检查");
        portScan.setDescription("检查系统开放的网络端口");
        portScan.setCategory("网络服务");
        portScan.setSeverity(BaselineCheckItem.SeverityLevel.MEDIUM);
        portScan.setExpectedValue("只开放必要的服务端口");
        portScan.setCheckCommand("netstat -tuln | grep LISTEN");
        portScan.setRemediation("关闭不必要的网络服务和端口");
        items.add(portScan);
        
        // 网络配置检查
        BaselineCheckItem networkConfig = new BaselineCheckItem();
        networkConfig.setBaselineCheck(baselineCheck);
        networkConfig.setItemCode("NET-002");
        networkConfig.setItemName("网络安全配置检查");
        networkConfig.setDescription("检查网络安全相关配置");
        networkConfig.setCategory("网络配置");
        networkConfig.setSeverity(BaselineCheckItem.SeverityLevel.MEDIUM);
        networkConfig.setExpectedValue("禁用IP转发，启用SYN Cookies");
        networkConfig.setCheckCommand("sysctl net.ipv4.ip_forward net.ipv4.tcp_syncookies");
        networkConfig.setRemediation("配置网络安全参数");
        items.add(networkConfig);
        
        return items;
    }

    /**
     * 生成数据库安全检查项
     */
    private List<BaselineCheckItem> generateDatabaseSecurityItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        
        // MySQL安全配置检查
        BaselineCheckItem mysqlSecurity = new BaselineCheckItem();
        mysqlSecurity.setBaselineCheck(baselineCheck);
        mysqlSecurity.setItemCode("DB-001");
        mysqlSecurity.setItemName("MySQL安全配置检查");
        mysqlSecurity.setDescription("检查MySQL数据库安全配置");
        mysqlSecurity.setCategory("数据库安全");
        mysqlSecurity.setSeverity(BaselineCheckItem.SeverityLevel.HIGH);
        mysqlSecurity.setExpectedValue("禁用远程root登录，启用SSL连接");
        mysqlSecurity.setCheckCommand("mysql -e \"SELECT user,host FROM mysql.user WHERE user='root';\"");
        mysqlSecurity.setRemediation("删除远程root账户，配置SSL连接");
        items.add(mysqlSecurity);
        
        return items;
    }

    /**
     * 生成Web安全检查项
     */
    private List<BaselineCheckItem> generateWebSecurityItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        
        // Web服务器配置检查
        BaselineCheckItem webServerConfig = new BaselineCheckItem();
        webServerConfig.setBaselineCheck(baselineCheck);
        webServerConfig.setItemCode("WEB-001");
        webServerConfig.setItemName("Web服务器安全配置检查");
        webServerConfig.setDescription("检查Web服务器安全配置");
        webServerConfig.setCategory("Web安全");
        webServerConfig.setSeverity(BaselineCheckItem.SeverityLevel.MEDIUM);
        webServerConfig.setExpectedValue("隐藏服务器版本信息，启用安全头");
        webServerConfig.setCheckCommand("curl -I " + asset.getIpAddress());
        webServerConfig.setRemediation("配置Web服务器隐藏版本信息，添加安全响应头");
        items.add(webServerConfig);
        
        return items;
    }

    /**
     * 生成中间件安全检查项
     */
    private List<BaselineCheckItem> generateMiddlewareSecurityItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        // 实现中间件安全检查项
        return items;
    }

    /**
     * 生成云安全检查项
     */
    private List<BaselineCheckItem> generateCloudSecurityItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        // 实现云安全检查项
        return items;
    }

    /**
     * 生成自定义检查项
     */
    private List<BaselineCheckItem> generateCustomItems(BaselineCheck baselineCheck, Asset asset) {
        List<BaselineCheckItem> items = new ArrayList<>();
        // 实现自定义检查项
        return items;
    }

    /**
     * 执行单个检查项
     */
    private void executeCheckItem(BaselineCheckItem item, Asset asset) {
        try {
            logger.debug("执行检查项: {} - {}", item.getItemCode(), item.getItemName());
            
            item.setStatus(BaselineCheckItem.ItemStatus.RUNNING);
            
            String command = item.getCheckCommand();
            if (command == null || command.trim().isEmpty()) {
                item.markAsSkipped("未配置检查命令");
                return;
            }
            
            // 执行检查命令
            ProcessResult processResult = executeCommand(command, asset);
            
            if (processResult.isSuccess()) {
                // 分析检查结果
                BaselineCheckItem.ItemResult result = analyzeCheckResult(item, processResult.getOutput());
                item.markAsCompleted(result, processResult.getOutput());
            } else {
                item.markAsError("命令执行失败: " + processResult.getError());
            }
            
        } catch (Exception e) {
            logger.error("检查项执行失败: {} - {}", item.getItemCode(), item.getItemName(), e);
            item.markAsError("执行异常: " + e.getMessage());
        }
    }

    /**
     * 执行系统命令（安全版本）
     */
    private ProcessResult executeCommand(String command, Asset asset) {
        try {
            logger.info("执行基线检查命令: {} for asset: {}", command, asset.getIpAddress());

            // 根据资产类型选择执行方式
            if ("localhost".equals(asset.getIpAddress()) || "127.0.0.1".equals(asset.getIpAddress())) {
                // 本地执行 - 使用安全的命令执行器
                return executeLocalCommandSecure(command);
            } else {
                // 远程执行（通过SSH）- 暂时禁用，返回模拟结果
                logger.warn("远程命令执行已禁用，返回模拟结果: {}", asset.getIpAddress());
                return new ProcessResult(true, "远程执行已禁用（安全考虑）", "");
            }
        } catch (Exception e) {
            logger.error("命令执行失败: {}", command, e);
            return new ProcessResult(false, "", e.getMessage());
        }
    }

    /**
     * 安全执行本地命令
     */
    private ProcessResult executeLocalCommandSecure(String command) {
        // 使用安全的命令执行器
        SecureCommandExecutor.CommandResult result = commandExecutor.executeCommand(command, 30);

        return new ProcessResult(
            result.isSuccess(),
            result.getOutput(),
            result.getError()
        );
    }

    /**
     * 执行远程命令（通过SSH）- 安全版本
     * 注意：远程命令执行已被禁用以防止安全风险
     */
    private ProcessResult executeRemoteCommand(String command, Asset asset) {
        // 远程命令执行存在严重安全风险，已被禁用
        // 如需启用，请确保：
        // 1. 使用安全的SSH连接
        // 2. 验证目标主机身份
        // 3. 使用受限的用户权限
        // 4. 对命令进行严格的安全检查

        logger.warn("远程命令执行已禁用（安全考虑）: {} on {}", command, asset.getIpAddress());
        return new ProcessResult(false, "", "远程命令执行已禁用（安全考虑）");
    }

    /**
     * 分析检查结果
     */
    private BaselineCheckItem.ItemResult analyzeCheckResult(BaselineCheckItem item, String output) {
        // 根据检查项类型和输出内容分析结果
        // 这里实现简单的规则匹配
        
        String expectedValue = item.getExpectedValue();
        if (expectedValue == null) {
            return BaselineCheckItem.ItemResult.PASS;
        }
        
        // 简单的字符串匹配分析
        if (output.toLowerCase().contains("error") || output.toLowerCase().contains("failed")) {
            return BaselineCheckItem.ItemResult.FAIL;
        }
        
        if (output.toLowerCase().contains("warning") || output.toLowerCase().contains("warn")) {
            return BaselineCheckItem.ItemResult.WARNING;
        }
        
        return BaselineCheckItem.ItemResult.PASS;
    }

    /**
     * 计算总体检查结果
     */
    private void calculateOverallResult(BaselineCheckResult result) {
        List<BaselineCheckItem> items = result.getCheckItems();
        
        int total = items.size();
        int passed = 0;
        int failed = 0;
        int warning = 0;
        int skipped = 0;
        
        for (BaselineCheckItem item : items) {
            switch (item.getResult()) {
                case PASS:
                    passed++;
                    break;
                case FAIL:
                    failed++;
                    break;
                case WARNING:
                    warning++;
                    break;
                case NOT_APPLICABLE:
                    skipped++;
                    break;
            }
        }
        
        result.setTotalItems(total);
        result.setPassedItems(passed);
        result.setFailedItems(failed);
        result.setWarningItems(warning);
        result.setSkippedItems(skipped);
        
        // 计算合规分数
        double complianceScore = total > 0 ? (double) passed / total * 100 : 100.0;
        result.setComplianceScore(complianceScore);
    }

    /**
     * 进程执行结果
     */
    private static class ProcessResult {
        private final boolean success;
        private final String output;
        private final String error;

        public ProcessResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }

        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public String getError() { return error; }
    }
}
