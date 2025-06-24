-- 创建资产检测记录表
CREATE TABLE asset_detections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    type VARCHAR(50) NOT NULL COMMENT '检测类型',
    status VARCHAR(50) NOT NULL COMMENT '检测状态',
    result VARCHAR(50) COMMENT '检测结果',
    target VARCHAR(500) NOT NULL COMMENT '目标地址',
    port INT COMMENT '端口号',
    response_time BIGINT COMMENT '响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    details TEXT COMMENT '检测详情',
    http_status_code INT COMMENT 'HTTP状态码',
    banner TEXT COMMENT '服务横幅信息',
    start_time DATETIME NOT NULL COMMENT '检测开始时间',
    end_time DATETIME COMMENT '检测结束时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_asset_id (asset_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_result (result),
    INDEX idx_created_time (created_time),
    INDEX idx_asset_type (asset_id, type),
    INDEX idx_target (target(100))
) COMMENT='资产检测记录表';

-- 创建资产指纹识别表
CREATE TABLE asset_fingerprints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    type VARCHAR(50) NOT NULL COMMENT '指纹类型',
    name VARCHAR(200) NOT NULL COMMENT '识别到的技术/产品名称',
    version VARCHAR(100) COMMENT '版本信息',
    vendor VARCHAR(100) COMMENT '厂商信息',
    confidence INT NOT NULL COMMENT '置信度（0-100）',
    method VARCHAR(50) COMMENT '识别方法',
    signature TEXT COMMENT '特征信息',
    port INT COMMENT '端口号',
    protocol VARCHAR(20) COMMENT '协议',
    banner TEXT COMMENT '服务横幅',
    http_headers TEXT COMMENT 'HTTP响应头',
    page_title VARCHAR(500) COMMENT '页面标题',
    error_page TEXT COMMENT '错误页面信息',
    signature_file VARCHAR(500) COMMENT '特征文件路径',
    extra_info TEXT COMMENT '额外信息',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否活跃',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_asset_id (asset_id),
    INDEX idx_type (type),
    INDEX idx_name (name),
    INDEX idx_vendor (vendor),
    INDEX idx_confidence (confidence),
    INDEX idx_method (method),
    INDEX idx_port (port),
    INDEX idx_active (active),
    INDEX idx_created_time (created_time),
    INDEX idx_asset_type (asset_id, type),
    INDEX idx_asset_name (asset_id, name),
    UNIQUE KEY uk_asset_type_name (asset_id, type, name)
) COMMENT='资产指纹识别表';

-- 创建资产标签表（如果不存在）
CREATE TABLE IF NOT EXISTS asset_tags (
    asset_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (asset_id, tag),
    INDEX idx_tag (tag)
) COMMENT='资产标签表';

-- 添加外键约束（如果需要）
-- ALTER TABLE asset_detections ADD CONSTRAINT fk_detection_asset 
--     FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE;

-- ALTER TABLE asset_fingerprints ADD CONSTRAINT fk_fingerprint_asset 
--     FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE;

-- 插入一些示例检测类型数据（可选）
INSERT IGNORE INTO asset_detections (asset_id, type, status, result, target, port, response_time, details, start_time, created_time) VALUES
(1, 'PING', 'COMPLETED', 'ONLINE', '127.0.0.1', NULL, 10, 'PING成功，响应时间: 10ms', NOW(), NOW()),
(1, 'TCP_PORT', 'COMPLETED', 'ONLINE', '127.0.0.1', 80, 50, '端口 80 开放，响应时间: 50ms', NOW(), NOW()),
(1, 'HTTP_SERVICE', 'COMPLETED', 'ONLINE', '127.0.0.1', 80, 100, 'HTTP服务正常，状态码: 200，响应时间: 100ms', NOW(), NOW());

-- 插入一些示例指纹数据（可选）
INSERT IGNORE INTO asset_fingerprints (asset_id, type, name, version, vendor, confidence, method, signature, port, protocol, active) VALUES
(1, 'WEB_SERVER', 'Apache HTTP Server', '2.4.41', 'Apache Software Foundation', 90, 'HTTP_HEADER', 'Server: Apache/2.4.41', 80, 'HTTP', TRUE),
(1, 'PROGRAMMING_LANGUAGE', 'PHP', '7.4.3', 'PHP Group', 85, 'HTTP_HEADER', 'X-Powered-By: PHP/7.4.3', 80, 'HTTP', TRUE),
(1, 'OPERATING_SYSTEM', 'Ubuntu Linux', '20.04', 'Canonical', 75, 'SERVER_BANNER', 'Ubuntu 20.04 LTS', NULL, NULL, TRUE);

-- 创建检测任务调度表（用于定时检测）
CREATE TABLE asset_detection_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    schedule_type VARCHAR(50) NOT NULL COMMENT '调度类型：ONCE, DAILY, WEEKLY, MONTHLY',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    next_run_time DATETIME COMMENT '下次执行时间',
    last_run_time DATETIME COMMENT '上次执行时间',
    include_fingerprint BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否包含指纹识别',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_by BIGINT COMMENT '创建者ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_asset_id (asset_id),
    INDEX idx_schedule_type (schedule_type),
    INDEX idx_next_run_time (next_run_time),
    INDEX idx_enabled (enabled),
    INDEX idx_created_by (created_by)
) COMMENT='资产检测调度表';

-- 创建检测配置表
CREATE TABLE asset_detection_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    description TEXT COMMENT '配置描述',
    detection_types JSON COMMENT '检测类型列表',
    timeout_seconds INT NOT NULL DEFAULT 30 COMMENT '超时时间（秒）',
    retry_count INT NOT NULL DEFAULT 3 COMMENT '重试次数',
    concurrent_limit INT NOT NULL DEFAULT 10 COMMENT '并发限制',
    include_fingerprint BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否包含指纹识别',
    custom_ports JSON COMMENT '自定义端口列表',
    user_agent VARCHAR(200) DEFAULT 'VulnArk-Scanner/1.0' COMMENT '用户代理',
    follow_redirects BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否跟随重定向',
    verify_ssl BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否验证SSL证书',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为默认配置',
    created_by BIGINT COMMENT '创建者ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_name (name),
    INDEX idx_is_default (is_default),
    INDEX idx_created_by (created_by)
) COMMENT='资产检测配置表';

-- 插入默认检测配置
INSERT IGNORE INTO asset_detection_configs (name, description, detection_types, timeout_seconds, retry_count, concurrent_limit, include_fingerprint, is_default) VALUES
('默认配置', '系统默认的资产检测配置', '["PING", "TCP_PORT", "HTTP_SERVICE", "HTTPS_SERVICE"]', 30, 3, 10, TRUE, TRUE),
('快速检测', '快速检测配置，只进行基础连通性检测', '["PING", "TCP_PORT"]', 10, 1, 20, FALSE, FALSE),
('深度检测', '深度检测配置，包含所有检测类型和指纹识别', '["PING", "TCP_PORT", "HTTP_SERVICE", "HTTPS_SERVICE", "SSH_SERVICE", "DATABASE_SERVICE"]', 60, 5, 5, TRUE, FALSE);
