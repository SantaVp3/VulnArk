-- 更新扫描任务表结构
ALTER TABLE scan_tasks 
ADD COLUMN scan_engine_type VARCHAR(50) DEFAULT 'INTERNAL' AFTER type,
ADD COLUMN scan_template VARCHAR(50) AFTER scan_engine_type,
ADD COLUMN target_count INT DEFAULT 0 AFTER scan_template,
ADD COLUMN high_risk_count INT DEFAULT 0 AFTER vulnerability_count,
ADD COLUMN medium_risk_count INT DEFAULT 0 AFTER high_risk_count,
ADD COLUMN low_risk_count INT DEFAULT 0 AFTER medium_risk_count,
ADD COLUMN info_risk_count INT DEFAULT 0 AFTER low_risk_count,
ADD COLUMN scan_config_id BIGINT AFTER info_risk_count,
ADD COLUMN external_task_id VARCHAR(100) AFTER scan_config_id,
ADD COLUMN scan_parameters TEXT AFTER external_task_id,
ADD COLUMN result_file_path VARCHAR(500) AFTER scan_parameters,
ADD COLUMN scheduled_start_time DATETIME AFTER result_file_path,
ADD COLUMN actual_start_time DATETIME AFTER scheduled_start_time,
ADD COLUMN completed_time DATETIME AFTER actual_start_time;

-- 修改现有列
ALTER TABLE scan_tasks 
MODIFY COLUMN name VARCHAR(200) NOT NULL,
MODIFY COLUMN type VARCHAR(50) NOT NULL,
MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'CREATED';

-- 添加索引
ALTER TABLE scan_tasks 
ADD INDEX idx_scan_engine_type (scan_engine_type),
ADD INDEX idx_scan_template (scan_template),
ADD INDEX idx_scan_config_id (scan_config_id),
ADD INDEX idx_external_task_id (external_task_id),
ADD INDEX idx_scheduled_start_time (scheduled_start_time),
ADD INDEX idx_actual_start_time (actual_start_time),
ADD INDEX idx_completed_time (completed_time);

-- 创建扫描任务目标关联表
CREATE TABLE IF NOT EXISTS scan_task_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_task_id BIGINT NOT NULL COMMENT '扫描任务ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '扫描状态',
    progress INT DEFAULT 0 COMMENT '扫描进度（0-100）',
    vulnerability_count INT DEFAULT 0 COMMENT '发现的漏洞数量',
    high_risk_count INT DEFAULT 0 COMMENT '高危漏洞数量',
    medium_risk_count INT DEFAULT 0 COMMENT '中危漏洞数量',
    low_risk_count INT DEFAULT 0 COMMENT '低危漏洞数量',
    info_risk_count INT DEFAULT 0 COMMENT '信息级漏洞数量',
    start_time DATETIME COMMENT '开始时间',
    completed_time DATETIME COMMENT '完成时间',
    error_message TEXT COMMENT '错误信息',
    result_summary TEXT COMMENT '扫描结果摘要',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_scan_task_id (scan_task_id),
    INDEX idx_asset_id (asset_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_completed_time (completed_time),
    UNIQUE KEY uk_scan_task_asset (scan_task_id, asset_id)
) COMMENT='扫描任务目标关联表';

-- 创建扫描配置表
CREATE TABLE IF NOT EXISTS scan_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    description TEXT COMMENT '配置描述',
    scan_engine VARCHAR(50) NOT NULL COMMENT '扫描引擎',
    scan_type VARCHAR(50) NOT NULL COMMENT '扫描类型',
    scan_template VARCHAR(50) COMMENT '扫描模板',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为默认配置',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    parameters TEXT COMMENT '扫描参数配置',
    timeout_minutes INT DEFAULT 60 COMMENT '超时时间（分钟）',
    max_concurrency INT DEFAULT 5 COMMENT '最大并发数',
    port_range VARCHAR(200) COMMENT '端口范围',
    exclude_ports VARCHAR(200) COMMENT '排除的端口',
    scan_depth VARCHAR(50) DEFAULT 'NORMAL' COMMENT '扫描深度',
    scan_udp BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否扫描UDP端口',
    service_detection BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否进行服务识别',
    os_detection BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否进行操作系统识别',
    script_scan BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否进行脚本扫描',
    vulnerability_scan BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否进行漏洞扫描',
    web_app_scan BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否进行Web应用扫描',
    custom_scripts TEXT COMMENT '自定义扫描脚本',
    exclude_vuln_types TEXT COMMENT '排除的漏洞类型',
    include_vuln_types TEXT COMMENT '包含的漏洞类型',
    scan_policy TEXT COMMENT '扫描策略',
    created_by BIGINT COMMENT '创建者ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标记',
    
    UNIQUE KEY uk_name (name),
    INDEX idx_scan_engine (scan_engine),
    INDEX idx_scan_type (scan_type),
    INDEX idx_scan_template (scan_template),
    INDEX idx_is_default (is_default),
    INDEX idx_enabled (enabled),
    INDEX idx_created_by (created_by),
    INDEX idx_deleted (deleted)
) COMMENT='扫描配置表';

-- 更新漏洞表结构，添加扫描相关字段
ALTER TABLE vulnerabilities 
ADD COLUMN asset_id BIGINT AFTER project_id,
ADD COLUMN scan_task_id BIGINT AFTER asset_id,
ADD COLUMN source VARCHAR(50) DEFAULT 'MANUAL' AFTER solution,
ADD COLUMN scan_engine VARCHAR(50) AFTER source,
ADD COLUMN plugin_id VARCHAR(100) AFTER scan_engine,
ADD COLUMN port INT AFTER plugin_id,
ADD COLUMN protocol VARCHAR(20) AFTER port,
ADD COLUMN url VARCHAR(500) AFTER protocol,
ADD COLUMN proof TEXT AFTER url,
ADD COLUMN reference_links TEXT AFTER proof;

-- 添加漏洞表索引
ALTER TABLE vulnerabilities 
ADD INDEX idx_asset_id (asset_id),
ADD INDEX idx_scan_task_id (scan_task_id),
ADD INDEX idx_source (source),
ADD INDEX idx_scan_engine (scan_engine),
ADD INDEX idx_plugin_id (plugin_id),
ADD INDEX idx_port (port),
ADD INDEX idx_protocol (protocol);

-- 插入默认扫描配置
INSERT IGNORE INTO scan_configs (name, description, scan_engine, scan_type, scan_template, is_default, enabled, 
                                 timeout_minutes, max_concurrency, port_range, scan_depth, service_detection, 
                                 os_detection, vulnerability_scan) VALUES
('默认端口扫描', '使用Nmap进行基础端口扫描', 'NMAP', 'PORT_SCAN', 'QUICK_SCAN', TRUE, TRUE, 
 30, 10, '1-1000', 'NORMAL', TRUE, FALSE, FALSE),
 
('默认漏洞扫描', '使用内置引擎进行漏洞扫描', 'INTERNAL', 'SYSTEM_SCAN', 'FULL_SCAN', TRUE, TRUE, 
 120, 5, '1-65535', 'DEEP', TRUE, TRUE, TRUE),
 
('Web应用扫描', '专门用于Web应用的安全扫描', 'INTERNAL', 'WEB_SCAN', 'WEB_APP_SCAN', FALSE, TRUE, 
 180, 3, '80,443,8080,8443', 'COMPREHENSIVE', TRUE, FALSE, TRUE),
 
('快速扫描', '快速端口和服务扫描', 'NMAP', 'PORT_SCAN', 'QUICK_SCAN', FALSE, TRUE, 
 15, 20, '1-1000', 'LIGHT', TRUE, FALSE, FALSE),
 
('全面扫描', '包含所有检测项的全面扫描', 'INTERNAL', 'COMPREHENSIVE_SCAN', 'FULL_SCAN', FALSE, TRUE, 
 300, 2, '1-65535', 'COMPREHENSIVE', TRUE, TRUE, TRUE);

-- 创建扫描任务队列表（用于任务调度）
CREATE TABLE IF NOT EXISTS scan_task_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_task_id BIGINT NOT NULL COMMENT '扫描任务ID',
    priority INT NOT NULL DEFAULT 5 COMMENT '优先级（1-10，数字越大优先级越高）',
    queue_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入队时间',
    start_time DATETIME COMMENT '开始处理时间',
    worker_id VARCHAR(100) COMMENT '处理工作者ID',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retries INT DEFAULT 3 COMMENT '最大重试次数',
    status VARCHAR(50) NOT NULL DEFAULT 'QUEUED' COMMENT '队列状态',
    
    INDEX idx_scan_task_id (scan_task_id),
    INDEX idx_priority (priority),
    INDEX idx_queue_time (queue_time),
    INDEX idx_status (status),
    INDEX idx_worker_id (worker_id)
) COMMENT='扫描任务队列表';

-- 创建扫描结果文件表
CREATE TABLE IF NOT EXISTS scan_result_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_task_id BIGINT NOT NULL COMMENT '扫描任务ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型（XML, JSON, CSV等）',
    file_size BIGINT COMMENT '文件大小（字节）',
    content_type VARCHAR(100) COMMENT '内容类型',
    is_processed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已处理',
    processed_time DATETIME COMMENT '处理时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_scan_task_id (scan_task_id),
    INDEX idx_file_type (file_type),
    INDEX idx_is_processed (is_processed),
    INDEX idx_created_time (created_time)
) COMMENT='扫描结果文件表';

-- 创建扫描统计表
CREATE TABLE IF NOT EXISTS scan_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_tasks INT DEFAULT 0 COMMENT '总任务数',
    completed_tasks INT DEFAULT 0 COMMENT '完成任务数',
    failed_tasks INT DEFAULT 0 COMMENT '失败任务数',
    total_vulnerabilities INT DEFAULT 0 COMMENT '总漏洞数',
    high_risk_vulnerabilities INT DEFAULT 0 COMMENT '高危漏洞数',
    medium_risk_vulnerabilities INT DEFAULT 0 COMMENT '中危漏洞数',
    low_risk_vulnerabilities INT DEFAULT 0 COMMENT '低危漏洞数',
    avg_scan_duration DECIMAL(10,2) COMMENT '平均扫描时长（分钟）',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_stat_date (stat_date),
    INDEX idx_stat_date (stat_date)
) COMMENT='扫描统计表';
