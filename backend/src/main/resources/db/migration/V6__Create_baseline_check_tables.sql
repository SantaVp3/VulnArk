-- 创建基线检查表
CREATE TABLE baseline_checks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '检查名称',
    description TEXT COMMENT '检查描述',
    check_type ENUM('SYSTEM_SECURITY', 'NETWORK_SECURITY', 'DATABASE_SECURITY', 'WEB_SECURITY', 'MIDDLEWARE_SECURITY', 'CLOUD_SECURITY', 'CUSTOM') NOT NULL COMMENT '检查类型',
    asset_id BIGINT COMMENT '目标资产ID',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '检查状态',
    result ENUM('PASS', 'FAIL', 'WARNING', 'PARTIAL') COMMENT '检查结果',
    progress INT DEFAULT 0 COMMENT '检查进度(0-100)',
    total_items INT DEFAULT 0 COMMENT '总检查项数',
    passed_items INT DEFAULT 0 COMMENT '通过项数',
    failed_items INT DEFAULT 0 COMMENT '失败项数',
    warning_items INT DEFAULT 0 COMMENT '警告项数',
    skipped_items INT DEFAULT 0 COMMENT '跳过项数',
    compliance_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '合规分数(0-100)',
    check_config TEXT COMMENT '检查配置',
    report_path VARCHAR(500) COMMENT '检查报告路径',
    error_message TEXT COMMENT '错误信息',
    created_by BIGINT COMMENT '创建者ID',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '完成时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_baseline_checks_asset_id (asset_id),
    INDEX idx_baseline_checks_check_type (check_type),
    INDEX idx_baseline_checks_status (status),
    INDEX idx_baseline_checks_created_by (created_by),
    INDEX idx_baseline_checks_created_time (created_time),
    INDEX idx_baseline_checks_deleted (deleted),
    
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='基线检查表';

-- 创建基线检查项表
CREATE TABLE baseline_check_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    baseline_check_id BIGINT NOT NULL COMMENT '基线检查ID',
    item_code VARCHAR(50) NOT NULL COMMENT '检查项编号',
    item_name VARCHAR(200) NOT NULL COMMENT '检查项名称',
    description TEXT COMMENT '检查项描述',
    category VARCHAR(100) COMMENT '检查分类',
    severity ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO') NOT NULL DEFAULT 'MEDIUM' COMMENT '严重级别',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'SKIPPED', 'ERROR') NOT NULL DEFAULT 'PENDING' COMMENT '检查状态',
    result ENUM('PASS', 'FAIL', 'WARNING', 'NOT_APPLICABLE') COMMENT '检查结果',
    expected_value TEXT COMMENT '期望值',
    actual_value TEXT COMMENT '实际值',
    check_command TEXT COMMENT '检查命令/脚本',
    check_details TEXT COMMENT '检查详情',
    remediation TEXT COMMENT '修复建议',
    reference VARCHAR(500) COMMENT '参考链接',
    error_message TEXT COMMENT '错误信息',
    check_time DATETIME COMMENT '检查时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_baseline_check_items_baseline_check_id (baseline_check_id),
    INDEX idx_baseline_check_items_item_code (item_code),
    INDEX idx_baseline_check_items_category (category),
    INDEX idx_baseline_check_items_severity (severity),
    INDEX idx_baseline_check_items_status (status),
    INDEX idx_baseline_check_items_result (result),
    INDEX idx_baseline_check_items_check_time (check_time),
    
    FOREIGN KEY (baseline_check_id) REFERENCES baseline_checks(id) ON DELETE CASCADE
) COMMENT='基线检查项表';

-- 插入示例数据
INSERT INTO baseline_checks (name, description, check_type, asset_id, created_by) VALUES
('系统安全基线检查', 'Linux系统安全配置基线检查', 'SYSTEM_SECURITY', 1, 1),
('网络安全基线检查', '网络服务和配置安全检查', 'NETWORK_SECURITY', 1, 1),
('Web应用安全基线检查', 'Web应用安全配置检查', 'WEB_SECURITY', 2, 1);

-- 为第一个基线检查插入示例检查项
INSERT INTO baseline_check_items (baseline_check_id, item_code, item_name, description, category, severity, expected_value, check_command, remediation) VALUES
(1, 'SYS-001', '密码复杂度策略检查', '检查系统密码复杂度策略配置', '身份认证', 'HIGH', '密码长度>=8位，包含大小写字母、数字和特殊字符', 'cat /etc/pam.d/common-password | grep pam_pwquality', '配置/etc/pam.d/common-password文件，启用密码复杂度检查'),
(1, 'SYS-002', 'SSH安全配置检查', '检查SSH服务安全配置', '网络服务', 'HIGH', '禁用root登录，使用密钥认证', 'grep -E "^(PermitRootLogin|PasswordAuthentication)" /etc/ssh/sshd_config', '修改/etc/ssh/sshd_config，设置PermitRootLogin no，PasswordAuthentication no'),
(1, 'SYS-003', '防火墙状态检查', '检查系统防火墙是否启用', '网络安全', 'MEDIUM', '防火墙服务已启用并运行', 'systemctl is-active ufw || systemctl is-active firewalld', '启用并配置防火墙服务'),
(1, 'SYS-004', '系统更新状态检查', '检查系统是否有可用的安全更新', '系统维护', 'MEDIUM', '系统已安装最新安全更新', 'apt list --upgradable 2>/dev/null | grep -i security || yum check-update --security', '安装可用的安全更新'),
(1, 'SYS-005', '关键文件权限检查', '检查关键系统文件的权限设置', '文件系统', 'HIGH', '/etc/passwd权限为644，/etc/shadow权限为640', 'ls -l /etc/passwd /etc/shadow', '设置正确的文件权限：chmod 644 /etc/passwd; chmod 640 /etc/shadow');

-- 为第二个基线检查插入示例检查项
INSERT INTO baseline_check_items (baseline_check_id, item_code, item_name, description, category, severity, expected_value, check_command, remediation) VALUES
(2, 'NET-001', '开放端口检查', '检查系统开放的网络端口', '网络服务', 'MEDIUM', '只开放必要的服务端口', 'netstat -tuln | grep LISTEN', '关闭不必要的网络服务和端口'),
(2, 'NET-002', '网络安全配置检查', '检查网络安全相关配置', '网络配置', 'MEDIUM', '禁用IP转发，启用SYN Cookies', 'sysctl net.ipv4.ip_forward net.ipv4.tcp_syncookies', '配置网络安全参数');

-- 为第三个基线检查插入示例检查项
INSERT INTO baseline_check_items (baseline_check_id, item_code, item_name, description, category, severity, expected_value, check_command, remediation) VALUES
(3, 'WEB-001', 'Web服务器安全配置检查', '检查Web服务器安全配置', 'Web安全', 'MEDIUM', '隐藏服务器版本信息，启用安全头', 'curl -I http://localhost', '配置Web服务器隐藏版本信息，添加安全响应头'),
(3, 'WEB-002', 'HTTPS配置检查', '检查HTTPS配置和证书', 'Web安全', 'HIGH', '启用HTTPS，使用有效证书', 'curl -I https://localhost', '配置HTTPS和有效的SSL证书'),
(3, 'WEB-003', 'Web应用防火墙检查', '检查Web应用防火墙配置', 'Web安全', 'MEDIUM', 'WAF已启用并正确配置', 'nginx -T | grep -i waf || apache2ctl -S | grep -i waf', '启用并配置Web应用防火墙');
