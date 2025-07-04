package com.vulnark.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 安全的命令执行器
 * 防止命令注入和RCE攻击
 */
@Component
public class SecureCommandExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureCommandExecutor.class);
    
    // 允许的安全命令白名单
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "ls", "cat", "grep", "ps", "netstat", "ss", "whoami", "id", "uname",
        "df", "free", "uptime", "date", "pwd", "which", "find", "head", "tail",
        "wc", "sort", "uniq", "cut", "awk", "sed", "systemctl", "service",
        "mysql", "curl", "wget", "ping", "nslookup", "dig", "telnet"
    );
    
    // 危险字符和模式
    private static final Set<String> DANGEROUS_CHARS = Set.of(
        ";", "&", "|", "&&", "||", "`", "$", "(", ")", "{", "}", 
        "<", ">", ">>", "<<", "*", "?", "[", "]", "~", "!"
    );
    
    // 危险命令模式
    private static final Set<Pattern> DANGEROUS_PATTERNS = Set.of(
        Pattern.compile(".*\\$\\(.*\\).*"),     // 命令替换 $()
        Pattern.compile(".*`.*`.*"),            // 命令替换 ``
        Pattern.compile(".*\\$\\{.*\\}.*"),     // 变量替换
        Pattern.compile(".*\\|\\|.*"),          // 逻辑或
        Pattern.compile(".*&&.*"),              // 逻辑与
        Pattern.compile(".*[;&|].*"),           // 命令分隔符
        Pattern.compile(".*>.*"),               // 重定向
        Pattern.compile(".*<.*"),               // 输入重定向
        Pattern.compile(".*/dev/.*"),           // 设备文件访问
        Pattern.compile(".*/proc/.*"),          // proc文件系统
        Pattern.compile(".*\\.\\./.*"),         // 路径遍历
        Pattern.compile(".*rm\\s+.*"),          // 删除命令
        Pattern.compile(".*chmod\\s+.*"),       // 权限修改
        Pattern.compile(".*chown\\s+.*"),       // 所有者修改
        Pattern.compile(".*sudo\\s+.*"),        // 提权命令
        Pattern.compile(".*su\\s+.*")           // 切换用户
    );
    
    // 命令执行结果
    public static class CommandResult {
        private final boolean success;
        private final String output;
        private final String error;
        private final int exitCode;
        
        public CommandResult(boolean success, String output, String error, int exitCode) {
            this.success = success;
            this.output = output;
            this.error = error;
            this.exitCode = exitCode;
        }
        
        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public String getError() { return error; }
        public int getExitCode() { return exitCode; }
    }
    
    /**
     * 安全执行命令
     */
    public CommandResult executeCommand(String command) {
        return executeCommand(command, 30); // 默认30秒超时
    }
    
    /**
     * 安全执行命令（带超时）
     */
    public CommandResult executeCommand(String command, int timeoutSeconds) {
        try {
            // 1. 验证命令安全性
            if (!isCommandSafe(command)) {
                return new CommandResult(false, "", "命令被安全策略拒绝", -1);
            }
            
            // 2. 解析和清理命令
            String[] cleanCommand = parseAndCleanCommand(command);
            if (cleanCommand == null) {
                return new CommandResult(false, "", "命令解析失败", -1);
            }
            
            // 3. 执行命令
            return executeCleanCommand(cleanCommand, timeoutSeconds);
            
        } catch (Exception e) {
            logger.error("命令执行异常: {}", command, e);
            return new CommandResult(false, "", "命令执行异常: " + e.getMessage(), -1);
        }
    }
    
    /**
     * 验证命令是否安全
     */
    private boolean isCommandSafe(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        String trimmedCommand = command.trim().toLowerCase();
        
        // 检查危险字符
        for (String dangerousChar : DANGEROUS_CHARS) {
            if (command.contains(dangerousChar)) {
                logger.warn("命令包含危险字符 '{}': {}", dangerousChar, command);
                return false;
            }
        }
        
        // 检查危险模式
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(command).matches()) {
                logger.warn("命令匹配危险模式: {}", command);
                return false;
            }
        }
        
        // 检查命令是否在白名单中
        String[] parts = trimmedCommand.split("\\s+");
        if (parts.length == 0) {
            return false;
        }
        
        String baseCommand = parts[0];
        if (!ALLOWED_COMMANDS.contains(baseCommand)) {
            logger.warn("命令不在白名单中: {}", baseCommand);
            return false;
        }
        
        return true;
    }
    
    /**
     * 解析和清理命令
     */
    private String[] parseAndCleanCommand(String command) {
        try {
            // 简单的空格分割，更复杂的解析可以使用专门的库
            String[] parts = command.trim().split("\\s+");
            
            // 验证每个参数
            for (String part : parts) {
                if (part.contains("..") || part.startsWith("/") && part.contains("..")) {
                    logger.warn("参数包含路径遍历: {}", part);
                    return null;
                }
            }
            
            return parts;
        } catch (Exception e) {
            logger.error("命令解析失败: {}", command, e);
            return null;
        }
    }
    
    /**
     * 执行清理后的命令
     */
    private CommandResult executeCleanCommand(String[] command, int timeoutSeconds) 
            throws IOException, InterruptedException {
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false); // 分别处理stdout和stderr
        
        // 设置安全的环境变量
        Map<String, String> env = pb.environment();
        env.clear(); // 清除所有环境变量
        env.put("PATH", "/usr/bin:/bin"); // 只保留基本PATH
        env.put("LANG", "C"); // 设置语言环境
        
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        
        // 读取输出
        try (BufferedReader outputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            
            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
                // 限制输出大小
                if (output.length() > 10000) {
                    output.append("... (输出被截断)");
                    break;
                }
            }
            
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
                // 限制错误输出大小
                if (error.length() > 5000) {
                    error.append("... (错误输出被截断)");
                    break;
                }
            }
        }
        
        // 等待进程完成
        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return new CommandResult(false, output.toString(), 
                "命令执行超时 (" + timeoutSeconds + "秒)", -1);
        }
        
        int exitCode = process.exitValue();
        boolean success = exitCode == 0;
        
        return new CommandResult(success, output.toString(), error.toString(), exitCode);
    }
    
    /**
     * 获取允许的命令列表（用于调试）
     */
    public Set<String> getAllowedCommands() {
        return new HashSet<>(ALLOWED_COMMANDS);
    }
}
