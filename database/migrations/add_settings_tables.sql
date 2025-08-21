-- 添加用户设置相关表
USE vulnark;

-- 系统设置表
CREATE TABLE IF NOT EXISTS system_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL UNIQUE COMMENT '用户ID',
    theme VARCHAR(20) DEFAULT 'system' COMMENT '主题设置：light, dark, system',
    language VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言设置：zh-CN, en-US',
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区设置',
    date_format VARCHAR(20) DEFAULT 'YYYY-MM-DD' COMMENT '日期格式',
    time_format VARCHAR(10) DEFAULT '24h' COMMENT '时间格式：12h, 24h',
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户系统设置表';

-- 通知设置表
CREATE TABLE IF NOT EXISTS notification_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL UNIQUE COMMENT '用户ID',
    
    -- 漏洞相关通知
    vuln_new_email BOOLEAN DEFAULT TRUE COMMENT '新漏洞发现-邮件通知',
    vuln_new_push BOOLEAN DEFAULT TRUE COMMENT '新漏洞发现-推送通知',
    vuln_new_sms BOOLEAN DEFAULT FALSE COMMENT '新漏洞发现-短信通知',
    vuln_assigned_email BOOLEAN DEFAULT TRUE COMMENT '漏洞分配-邮件通知',
    vuln_assigned_push BOOLEAN DEFAULT TRUE COMMENT '漏洞分配-推送通知',
    vuln_assigned_sms BOOLEAN DEFAULT TRUE COMMENT '漏洞分配-短信通知',
    vuln_fixed_email BOOLEAN DEFAULT TRUE COMMENT '漏洞修复-邮件通知',
    vuln_fixed_push BOOLEAN DEFAULT FALSE COMMENT '漏洞修复-推送通知',
    vuln_fixed_sms BOOLEAN DEFAULT FALSE COMMENT '漏洞修复-短信通知',
    
    -- 报告相关通知
    report_email BOOLEAN DEFAULT TRUE COMMENT '报告生成-邮件通知',
    report_push BOOLEAN DEFAULT FALSE COMMENT '报告生成-推送通知',
    report_sms BOOLEAN DEFAULT FALSE COMMENT '报告生成-短信通知',
    
    -- 系统相关通知
    system_email BOOLEAN DEFAULT TRUE COMMENT '系统维护-邮件通知',
    system_push BOOLEAN DEFAULT TRUE COMMENT '系统维护-推送通知',
    system_sms BOOLEAN DEFAULT FALSE COMMENT '系统维护-短信通知',
    
    -- 社交相关通知
    social_email BOOLEAN DEFAULT FALSE COMMENT '用户活动-邮件通知',
    social_push BOOLEAN DEFAULT TRUE COMMENT '用户活动-推送通知',
    social_sms BOOLEAN DEFAULT FALSE COMMENT '用户活动-短信通知',
    
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户通知设置表';

-- 用户会话表（用于会话管理）
CREATE TABLE IF NOT EXISTS user_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL COMMENT '用户ID',
    session_token VARCHAR(255) NOT NULL UNIQUE COMMENT '会话令牌',
    device_info VARCHAR(500) COMMENT '设备信息',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '登录位置',
    user_agent TEXT COMMENT '用户代理',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否活跃',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后活动时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_session_token (session_token),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户会话表';

-- 数据导出记录表
CREATE TABLE IF NOT EXISTS data_exports (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL COMMENT '用户ID',
    export_type VARCHAR(50) NOT NULL COMMENT '导出类型：profile, settings, assets, vulnerabilities, reports, all',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending, processing, completed, failed',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    error_message TEXT COMMENT '错误信息',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_export_type (export_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '数据导出记录表';

-- 系统维护日志表
CREATE TABLE IF NOT EXISTS system_maintenance_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL COMMENT '操作用户ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：cache_clear, db_optimize, backup, restore',
    operation_name VARCHAR(100) NOT NULL COMMENT '操作名称',
    description TEXT COMMENT '操作描述',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending, running, completed, failed',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration INT DEFAULT 0 COMMENT '耗时（秒）',
    result_data JSON COMMENT '结果数据',
    error_message TEXT COMMENT '错误信息',
    
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT '系统维护日志表';

-- 插入默认系统设置（为现有用户）
INSERT IGNORE INTO system_settings (user_id, theme, language, timezone, date_format, time_format)
SELECT id, 'system', 'zh-CN', 'Asia/Shanghai', 'YYYY-MM-DD', '24h'
FROM users;

-- 插入默认通知设置（为现有用户）
INSERT IGNORE INTO notification_settings (
    user_id, vuln_new_email, vuln_new_push, vuln_new_sms,
    vuln_assigned_email, vuln_assigned_push, vuln_assigned_sms,
    vuln_fixed_email, vuln_fixed_push, vuln_fixed_sms,
    report_email, report_push, report_sms,
    system_email, system_push, system_sms,
    social_email, social_push, social_sms
)
SELECT id, TRUE, TRUE, FALSE,
       TRUE, TRUE, TRUE,
       TRUE, FALSE, FALSE,
       TRUE, FALSE, FALSE,
       TRUE, TRUE, FALSE,
       FALSE, TRUE, FALSE
FROM users;
