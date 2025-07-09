-- VulnArk 完整数据库架构和数据
-- 此文件包含所有表结构、索引、外键约束和初始数据
-- 创建时间: 2024-01-01
-- 版本: 1.0

-- 创建数据库
CREATE DATABASE IF NOT EXISTS vulnark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vulnark;

-- ======================================
-- 基础表结构
-- ======================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    full_name VARCHAR(100) COMMENT '全名',
    phone VARCHAR(20) COMMENT '电话',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    role ENUM('ADMIN', 'MANAGER', 'ANALYST', 'VIEWER') NOT NULL DEFAULT 'VIEWER' COMMENT '角色',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    owner_id BIGINT NOT NULL COMMENT '项目负责人ID',
    status ENUM('ACTIVE', 'INACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '项目状态',
    type VARCHAR(50) DEFAULT 'General' COMMENT '项目类型',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    budget DECIMAL(15,2) DEFAULT 0.00 COMMENT '预算',
    tags VARCHAR(500) COMMENT '标签',
    member_count INT DEFAULT 0 COMMENT '成员数量',
    vulnerability_count INT DEFAULT 0 COMMENT '漏洞数量',
    asset_count INT DEFAULT 0 COMMENT '资产数量',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 资产表
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '资产名称',
    description TEXT COMMENT '资产描述',
    type ENUM('SERVER', 'WORKSTATION', 'NETWORK_DEVICE', 'DATABASE', 'WEB_APPLICATION', 'MOBILE_APPLICATION', 'IOT_DEVICE', 'CLOUD_SERVICE', 'OTHER') NOT NULL COMMENT '资产类型',
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'DECOMMISSIONED') NOT NULL DEFAULT 'ACTIVE' COMMENT '资产状态',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    domain VARCHAR(255) COMMENT '域名',
    port INT COMMENT '端口',
    protocol VARCHAR(20) COMMENT '协议',
    service VARCHAR(100) COMMENT '服务',
    version VARCHAR(50) COMMENT '版本',
    operating_system VARCHAR(100) COMMENT '操作系统',
    importance ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM' COMMENT '重要性等级',
    project_id BIGINT DEFAULT 1 COMMENT '项目ID',
    owner_id BIGINT COMMENT '负责人ID',
    location VARCHAR(200) COMMENT '位置',
    vendor VARCHAR(100) COMMENT '供应商',
    tags VARCHAR(500) COMMENT '资产标签',
    vulnerability_count INT DEFAULT 0 COMMENT '漏洞数量',
    risk_score DECIMAL(3,1) DEFAULT 0.0 COMMENT '风险评分',
    notes TEXT COMMENT '备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表';

-- 漏洞表
CREATE TABLE IF NOT EXISTS vulnerabilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '漏洞标题',
    description TEXT COMMENT '漏洞描述',
    severity ENUM('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '严重程度',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED') NOT NULL DEFAULT 'OPEN' COMMENT '状态',
    category VARCHAR(50) COMMENT '漏洞分类',
    cve_id VARCHAR(20) COMMENT 'CVE编号',
    cvss_score DECIMAL(3,1) COMMENT 'CVSS评分',
    project_id BIGINT DEFAULT 1 COMMENT '项目ID',
    asset_id BIGINT COMMENT '资产ID',
    scan_task_id BIGINT COMMENT '扫描任务ID',
    reporter_id BIGINT NOT NULL COMMENT '报告人ID',
    assignee_id BIGINT COMMENT '负责人ID',
    discovered_date DATE NOT NULL COMMENT '发现日期',
    due_date DATE COMMENT '修复截止日期',
    resolved_date DATE COMMENT '解决日期',
    verification_status ENUM('DISCOVERED', 'PENDING_FIX', 'FIXING', 'PENDING_RETEST', 'RETESTING', 'FIXED', 'VERIFIED', 'FALSE_POSITIVE', 'ACCEPTED_RISK', 'WONT_FIX') DEFAULT 'DISCOVERED' COMMENT '验证状态',
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') COMMENT '风险等级',
    affected_systems TEXT COMMENT '受影响系统',
    reproduction_steps TEXT COMMENT '复现步骤',
    solution TEXT COMMENT '解决方案',
    source VARCHAR(50) DEFAULT 'MANUAL' COMMENT '来源',
    scan_engine VARCHAR(50) COMMENT '扫描引擎',
    plugin_id VARCHAR(100) COMMENT '插件ID',
    port INT COMMENT '端口',
    protocol VARCHAR(20) COMMENT '协议',
    url VARCHAR(500) COMMENT 'URL',
    proof TEXT COMMENT '证据',
    reference_links TEXT COMMENT '参考链接',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (assignee_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='漏洞表';

-- 漏洞附件表
CREATE TABLE IF NOT EXISTS vulnerability_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vulnerability_id BIGINT NOT NULL COMMENT '漏洞ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小',
    file_type VARCHAR(50) COMMENT '文件类型',
    uploaded_by BIGINT NOT NULL COMMENT '上传者ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (vulnerability_id) REFERENCES vulnerabilities(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='漏洞附件表';

-- 漏洞评论表
CREATE TABLE IF NOT EXISTS vulnerability_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vulnerability_id BIGINT NOT NULL COMMENT '漏洞ID',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    content TEXT NOT NULL COMMENT '评论内容',
    comment_type ENUM('COMMENT', 'STATUS_CHANGE', 'ASSIGNMENT') DEFAULT 'COMMENT' COMMENT '评论类型',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (vulnerability_id) REFERENCES vulnerabilities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='漏洞评论表';

-- 漏洞历史记录表
CREATE TABLE IF NOT EXISTS vulnerability_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vulnerability_id BIGINT NOT NULL COMMENT '漏洞ID',
    user_id BIGINT NOT NULL COMMENT '操作者ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    old_value TEXT COMMENT '旧值',
    new_value TEXT COMMENT '新值',
    field_name VARCHAR(50) COMMENT '字段名',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (vulnerability_id) REFERENCES vulnerabilities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='漏洞历史记录表';

-- 漏洞状态变更表
CREATE TABLE IF NOT EXISTS vulnerability_status_changes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vulnerability_id BIGINT NOT NULL COMMENT '漏洞ID',
    old_status VARCHAR(50) COMMENT '原状态',
    new_status VARCHAR(50) NOT NULL COMMENT '新状态',
    reason TEXT COMMENT '变更原因',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '审批状态',
    requested_by BIGINT NOT NULL COMMENT '申请人ID',
    approved_by BIGINT COMMENT '审批人ID',
    requested_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    approved_time DATETIME COMMENT '审批时间',
    notes TEXT COMMENT '备注',
    FOREIGN KEY (vulnerability_id) REFERENCES vulnerabilities(id) ON DELETE CASCADE,
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='漏洞状态变更表';

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type ENUM('VULNERABILITY', 'SYSTEM') NOT NULL COMMENT '通知类型',
    status ENUM('UNREAD', 'read') NOT NULL DEFAULT 'UNREAD' COMMENT '状态',
    related_id BIGINT COMMENT '关联ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_time DATETIME COMMENT '阅读时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ======================================
-- 资产相关表
-- ======================================

-- 资产依赖关系表
CREATE TABLE IF NOT EXISTS asset_dependencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_asset_id BIGINT NOT NULL COMMENT '源资产ID（依赖方）',
    target_asset_id BIGINT NOT NULL COMMENT '目标资产ID（被依赖方）',
    dependency_type ENUM('NETWORK', 'DATABASE', 'SERVICE', 'APPLICATION', 'INFRASTRUCTURE', 'DATA_FLOW', 'AUTHENTICATION', 'STORAGE', 'MONITORING', 'BACKUP', 'OTHER') NOT NULL COMMENT '依赖类型',
    dependency_strength ENUM('WEAK', 'MEDIUM', 'STRONG', 'CRITICAL') NOT NULL COMMENT '依赖强度',
    description VARCHAR(500) COMMENT '依赖描述',
    port INT COMMENT '端口信息',
    protocol VARCHAR(50) COMMENT '协议信息',
    service_name VARCHAR(100) COMMENT '服务名称',
    is_critical TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为关键依赖',
    status ENUM('ACTIVE', 'INACTIVE', 'BROKEN', 'DEPRECATED') NOT NULL DEFAULT 'ACTIVE' COMMENT '依赖状态',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建者ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (source_asset_id) REFERENCES assets(id),
    FOREIGN KEY (target_asset_id) REFERENCES assets(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    UNIQUE KEY uk_asset_dependency (source_asset_id, target_asset_id, deleted),
    INDEX idx_asset_dependencies_source (source_asset_id),
    INDEX idx_asset_dependencies_target (target_asset_id),
    INDEX idx_asset_dependencies_type (dependency_type),
    INDEX idx_asset_dependencies_strength (dependency_strength),
    INDEX idx_asset_dependencies_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产依赖关系表';

-- 资产发现任务表
CREATE TABLE IF NOT EXISTS asset_discovery_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '任务描述',
    target_type ENUM('IP_RANGE', 'SUBNET', 'DOMAIN', 'URL_LIST', 'CUSTOM') NOT NULL COMMENT '目标类型',
    targets TEXT NOT NULL COMMENT '扫描目标（JSON格式）',
    discovery_type ENUM('PING_SWEEP', 'PORT_SCAN', 'SERVICE_DETECTION', 'FULL_DISCOVERY') NOT NULL COMMENT '发现类型',
    discovery_ports VARCHAR(1000) COMMENT '发现端口范围',
    discovery_options JSON COMMENT '发现选项配置',
    schedule_type ENUM('ONCE', 'DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM') NOT NULL DEFAULT 'ONCE' COMMENT '调度类型',
    schedule_config JSON COMMENT '调度配置',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
    progress DECIMAL(5,2) DEFAULT 0.00 COMMENT '执行进度',
    last_run_time DATETIME COMMENT '最后执行时间',
    next_run_time DATETIME COMMENT '下次执行时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_discovery_tasks_status (status),
    INDEX idx_discovery_tasks_schedule (schedule_type, next_run_time),
    INDEX idx_discovery_tasks_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产发现任务表';

-- 资产发现结果表
CREATE TABLE IF NOT EXISTS asset_discovery_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '发现任务ID',
    target VARCHAR(500) NOT NULL COMMENT '扫描目标',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    hostname VARCHAR(255) COMMENT '主机名',
    mac_address VARCHAR(17) COMMENT 'MAC地址',
    is_alive TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否在线',
    response_time INT COMMENT '响应时间(ms)',
    open_ports JSON COMMENT '开放端口列表',
    services JSON COMMENT '检测到的服务',
    operating_system VARCHAR(200) COMMENT '操作系统',
    device_type VARCHAR(100) COMMENT '设备类型',
    vendor VARCHAR(100) COMMENT '厂商信息',
    confidence_score DECIMAL(3,2) DEFAULT 0.00 COMMENT '识别置信度',
    raw_data JSON COMMENT '原始扫描数据',
    asset_id BIGINT COMMENT '关联的资产ID',
    correlation_status ENUM('NEW', 'MATCHED', 'UPDATED', 'IGNORED') NOT NULL DEFAULT 'NEW' COMMENT '关联状态',
    discovered_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
    FOREIGN KEY (task_id) REFERENCES asset_discovery_tasks(id),
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    INDEX idx_discovery_results_task (task_id),
    INDEX idx_discovery_results_ip (ip_address),
    INDEX idx_discovery_results_status (correlation_status),
    INDEX idx_discovery_results_time (discovered_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产发现结果表';

-- 资产检测记录表
CREATE TABLE IF NOT EXISTS asset_detections (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产检测记录表';

-- 资产指纹识别表
CREATE TABLE IF NOT EXISTS asset_fingerprints (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产指纹识别表';

-- 资产标签表
CREATE TABLE IF NOT EXISTS asset_tags (
    asset_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (asset_id, tag),
    INDEX idx_tag (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产标签表';

-- 资产检测调度表
CREATE TABLE IF NOT EXISTS asset_detection_schedules (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产检测调度表';

-- 资产检测配置表
CREATE TABLE IF NOT EXISTS asset_detection_configs (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产检测配置表';

-- ======================================
-- 扫描相关表
-- ======================================

-- 扫描任务表
CREATE TABLE IF NOT EXISTS scan_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '任务描述',
    type VARCHAR(50) NOT NULL COMMENT '扫描类型',
    scan_engine_type VARCHAR(50) DEFAULT 'INTERNAL' COMMENT '扫描引擎类型',
    scan_template VARCHAR(50) COMMENT '扫描模板',
    target_count INT DEFAULT 0 COMMENT '目标数量',
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED' COMMENT '任务状态',
    progress DECIMAL(5,2) DEFAULT 0.00 COMMENT '执行进度',
    vulnerability_count INT DEFAULT 0 COMMENT '发现的漏洞数量',
    high_risk_count INT DEFAULT 0 COMMENT '高危漏洞数量',
    medium_risk_count INT DEFAULT 0 COMMENT '中危漏洞数量',
    low_risk_count INT DEFAULT 0 COMMENT '低危漏洞数量',
    info_risk_count INT DEFAULT 0 COMMENT '信息级漏洞数量',
    scan_config_id BIGINT COMMENT '扫描配置ID',
    external_task_id VARCHAR(100) COMMENT '外部任务ID',
    scan_parameters TEXT COMMENT '扫描参数',
    result_file_path VARCHAR(500) COMMENT '结果文件路径',
    scheduled_start_time DATETIME COMMENT '计划开始时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    completed_time DATETIME COMMENT '完成时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建者ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_scan_tasks_type (type),
    INDEX idx_scan_tasks_status (status),
    INDEX idx_scan_tasks_created_by (created_by),
    INDEX idx_scan_tasks_created_time (created_time),
    INDEX idx_scan_engine_type (scan_engine_type),
    INDEX idx_scan_template (scan_template),
    INDEX idx_scan_config_id (scan_config_id),
    INDEX idx_external_task_id (external_task_id),
    INDEX idx_scheduled_start_time (scheduled_start_time),
    INDEX idx_actual_start_time (actual_start_time),
    INDEX idx_completed_time (completed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描任务表';

-- 扫描任务目标关联表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描任务目标关联表';

-- 扫描配置表
CREATE TABLE IF NOT EXISTS scan_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    description TEXT COMMENT '配置描述',
    scan_engine ENUM('NESSUS', 'OPENVAS', 'AWVS', 'NUCLEI', 'NMAP', 'INTERNAL') NOT NULL COMMENT '扫描引擎',
    scan_type ENUM('PORT_SCAN', 'WEB_SCAN', 'SYSTEM_SCAN', 'COMPREHENSIVE_SCAN', 'CUSTOM_SCAN') NOT NULL COMMENT '扫描类型',
    scan_template ENUM('QUICK_SCAN', 'BASIC_SCAN', 'FULL_SCAN', 'ADVANCED_SCAN', 'WEB_SCAN', 'API_SCAN', 'PORT_SCAN', 'COMPLIANCE', 'CUSTOM') COMMENT '扫描模板',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认配置',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    parameters TEXT COMMENT '扫描参数配置',
    timeout_minutes INT DEFAULT 60 COMMENT '超时时间（分钟）',
    max_concurrency INT DEFAULT 5 COMMENT '最大并发数',
    port_range VARCHAR(200) COMMENT '端口范围',
    exclude_ports VARCHAR(200) COMMENT '排除的端口',
    scan_depth ENUM('LIGHT', 'NORMAL', 'DEEP', 'COMPREHENSIVE') DEFAULT 'NORMAL' COMMENT '扫描深度',
    scan_udp BOOLEAN DEFAULT FALSE COMMENT '是否扫描UDP端口',
    service_detection BOOLEAN DEFAULT TRUE COMMENT '是否进行服务识别',
    os_detection BOOLEAN DEFAULT TRUE COMMENT '是否进行操作系统识别',
    script_scan BOOLEAN DEFAULT FALSE COMMENT '是否进行脚本扫描',
    vulnerability_scan BOOLEAN DEFAULT TRUE COMMENT '是否进行漏洞扫描',
    web_app_scan BOOLEAN DEFAULT FALSE COMMENT '是否进行Web应用扫描',
    custom_scripts TEXT COMMENT '自定义扫描脚本',
    exclude_vuln_types TEXT COMMENT '排除的漏洞类型',
    include_vuln_types TEXT COMMENT '包含的漏洞类型',
    scan_policy TEXT COMMENT '扫描策略',
    api_endpoint VARCHAR(500) COMMENT 'API端点URL',
    api_key VARCHAR(200) COMMENT 'API访问密钥',
    api_secret VARCHAR(200) COMMENT 'API密钥',
    api_username VARCHAR(100) COMMENT 'API用户名',
    api_password VARCHAR(200) COMMENT 'API密码',
    api_token VARCHAR(500) COMMENT 'API认证令牌',
    api_version VARCHAR(20) COMMENT 'API版本',
    ssl_verification BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用SSL验证',
    connection_timeout INT DEFAULT 30 COMMENT '连接超时时间（秒）',
    read_timeout INT DEFAULT 300 COMMENT '读取超时时间（秒）',
    extended_config TEXT COMMENT '扩展配置（JSON格式）',
    created_by BIGINT COMMENT '创建者ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记',
    CONSTRAINT uk_scan_configs_name UNIQUE (name, deleted),
    INDEX idx_scan_configs_name (name),
    INDEX idx_scan_configs_engine (scan_engine),
    INDEX idx_scan_configs_type (scan_type),
    INDEX idx_scan_configs_enabled (enabled),
    INDEX idx_scan_configs_default (is_default),
    INDEX idx_scan_configs_creator (created_by),
    INDEX idx_scan_configs_deleted (deleted),
    INDEX idx_scan_configs_created_time (created_time),
    INDEX idx_scan_configs_api_endpoint (api_endpoint),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扫描配置表';

-- 扫描任务队列表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描任务队列表';

-- 扫描结果文件表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描结果文件表';

-- 扫描统计表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描统计表';

-- ======================================
-- 基线检查相关表
-- ======================================

-- 基线检查表
CREATE TABLE IF NOT EXISTS baseline_checks (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线检查表';

-- 基线检查项表
CREATE TABLE IF NOT EXISTS baseline_check_items (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线检查项表';

-- 基线检查模板表
CREATE TABLE IF NOT EXISTS baseline_check_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_id VARCHAR(50) NOT NULL UNIQUE COMMENT '检查项ID',
    check_name VARCHAR(200) NOT NULL COMMENT '检查项名称',
    check_description TEXT COMMENT '检查项描述',
    system_type ENUM('WINDOWS', 'LINUX', 'UNIX', 'MACOS') NOT NULL COMMENT '系统类型',
    system_version VARCHAR(50) COMMENT '系统版本',
    category ENUM('SYSTEM_CONFIGURATION', 'USER_ACCOUNT', 'PASSWORD_POLICY', 'AUDIT_POLICY', 
                  'NETWORK_SECURITY', 'SERVICE_CONFIGURATION', 'FILE_PERMISSION', 'REGISTRY_SETTING',
                  'FIREWALL_CONFIGURATION', 'SOFTWARE_INSTALLATION', 'SYSTEM_UPDATE', 'LOG_CONFIGURATION',
                  'ENCRYPTION_SETTING', 'ACCESS_CONTROL', 'COMPLIANCE_CHECK') NOT NULL COMMENT '检查类别',
    severity ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO') NOT NULL COMMENT '严重程度',
    check_command TEXT NOT NULL COMMENT '检查命令',
    expected_value VARCHAR(500) COMMENT '期望值',
    comparison_operator VARCHAR(20) DEFAULT 'equals' COMMENT '比较操作符',
    remediation TEXT COMMENT '修复建议',
    reference VARCHAR(500) COMMENT '参考资料',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    timeout_seconds INT DEFAULT 30 COMMENT '超时时间(秒)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_system_type (system_type),
    INDEX idx_category (category),
    INDEX idx_severity (severity),
    INDEX idx_enabled (enabled),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线检查模板表';

-- 基线扫描表（合并V7和V11版本，以V11为准）
CREATE TABLE IF NOT EXISTS baseline_scans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_name VARCHAR(100) NOT NULL COMMENT '扫描名称',
    description VARCHAR(500) COMMENT '扫描描述',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    scan_type ENUM('WINDOWS_SERVER_2019', 'WINDOWS_SERVER_2022', 'WINDOWS_10', 'WINDOWS_11',
                   'UBUNTU_18_04', 'UBUNTU_20_04', 'UBUNTU_22_04', 'CENTOS_7', 'CENTOS_8',
                   'RHEL_7', 'RHEL_8', 'RHEL_9', 'DEBIAN_10', 'DEBIAN_11', 'SUSE_15', 'CUSTOM') NOT NULL COMMENT '扫描类型',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '扫描状态',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    total_checks INT DEFAULT 0 COMMENT '总检查项数',
    passed_checks INT DEFAULT 0 COMMENT '通过检查项数',
    failed_checks INT DEFAULT 0 COMMENT '失败检查项数',
    warning_checks INT DEFAULT 0 COMMENT '警告检查项数',
    compliance_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '合规得分',
    error_message TEXT COMMENT '错误信息',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    INDEX idx_asset_id (asset_id),
    INDEX idx_scan_type (scan_type),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线扫描表';

-- 基线扫描结果表（合并V7和V11版本，以V11为准）
CREATE TABLE IF NOT EXISTS baseline_scan_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    baseline_scan_id BIGINT NOT NULL COMMENT '基线扫描ID',
    check_id VARCHAR(50) NOT NULL COMMENT '检查项ID',
    check_name VARCHAR(200) NOT NULL COMMENT '检查项名称',
    check_description TEXT COMMENT '检查项描述',
    category ENUM('SYSTEM_CONFIGURATION', 'USER_ACCOUNT', 'PASSWORD_POLICY', 'AUDIT_POLICY', 
                  'NETWORK_SECURITY', 'SERVICE_CONFIGURATION', 'FILE_PERMISSION', 'REGISTRY_SETTING',
                  'FIREWALL_CONFIGURATION', 'SOFTWARE_INSTALLATION', 'SYSTEM_UPDATE', 'LOG_CONFIGURATION',
                  'ENCRYPTION_SETTING', 'ACCESS_CONTROL', 'COMPLIANCE_CHECK') NOT NULL COMMENT '检查类别',
    severity ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO') NOT NULL COMMENT '严重程度',
    status ENUM('PASS', 'FAIL', 'WARNING', 'NOT_APPLICABLE', 'ERROR') NOT NULL COMMENT '检查状态',
    expected_value VARCHAR(500) COMMENT '期望值',
    actual_value VARCHAR(500) COMMENT '实际值',
    check_command TEXT COMMENT '检查命令',
    remediation TEXT COMMENT '修复建议',
    reference VARCHAR(500) COMMENT '参考资料',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    error_message TEXT COMMENT '错误信息',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    FOREIGN KEY (baseline_scan_id) REFERENCES baseline_scans(id) ON DELETE CASCADE,
    INDEX idx_baseline_scan_id (baseline_scan_id),
    INDEX idx_check_id (check_id),
    INDEX idx_category (category),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线扫描结果表';

-- ======================================
-- 扫描工具和漏洞扫描相关表
-- ======================================

-- 扫描工具表
CREATE TABLE IF NOT EXISTS scan_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '工具名称',
    display_name VARCHAR(100) COMMENT '显示名称',
    current_version VARCHAR(20) COMMENT '当前版本',
    latest_version VARCHAR(20) COMMENT '最新版本',
    install_path VARCHAR(500) COMMENT '安装路径',
    config_path VARCHAR(500) COMMENT '配置文件路径',
    status VARCHAR(20) DEFAULT 'NOT_INSTALLED' COMMENT '工具状态',
    auto_update BOOLEAN DEFAULT TRUE COMMENT '自动更新',
    download_url VARCHAR(1000) COMMENT '下载地址',
    checksum VARCHAR(128) COMMENT '文件校验和',
    last_check_time DATETIME COMMENT '最后检查时间',
    last_update_time DATETIME COMMENT '最后更新时间',
    error_message TEXT COMMENT '错误信息',
    file_size BIGINT COMMENT '文件大小',
    download_progress INT DEFAULT 0 COMMENT '下载进度',
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描工具表';

-- 漏洞扫描表
CREATE TABLE IF NOT EXISTS vulnerability_scans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_name VARCHAR(255) NOT NULL COMMENT '扫描名称',
    description TEXT COMMENT '扫描描述',
    target_url VARCHAR(1000) NOT NULL COMMENT '目标URL',
    target_type VARCHAR(20) DEFAULT 'URL' COMMENT '目标类型',
    scan_tools JSON COMMENT '扫描工具配置',
    scan_config JSON COMMENT '扫描配置参数',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '扫描状态',
    progress INT DEFAULT 0 COMMENT '扫描进度',
    vulnerability_count INT DEFAULT 0 COMMENT '漏洞总数',
    high_risk_count INT DEFAULT 0 COMMENT '高危漏洞数',
    medium_risk_count INT DEFAULT 0 COMMENT '中危漏洞数',
    low_risk_count INT DEFAULT 0 COMMENT '低危漏洞数',
    info_count INT DEFAULT 0 COMMENT '信息级漏洞数',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_by BIGINT COMMENT '创建人ID',
    error_message TEXT COMMENT '错误信息',
    scan_duration BIGINT COMMENT '扫描耗时(秒)',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_target_url (target_url(255)),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    INDEX idx_deleted (deleted),
    CONSTRAINT fk_vulnerability_scans_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='漏洞扫描表';

-- 扫描结果表
CREATE TABLE IF NOT EXISTS scan_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_id BIGINT NOT NULL COMMENT '扫描ID',
    tool_name VARCHAR(50) NOT NULL COMMENT '扫描工具名称',
    vuln_type VARCHAR(100) COMMENT '漏洞类型',
    title VARCHAR(200) NOT NULL COMMENT '漏洞标题',
    url VARCHAR(500) NOT NULL COMMENT '漏洞URL',
    severity VARCHAR(20) NOT NULL COMMENT '严重程度: HIGH, MEDIUM, LOW, INFO',
    description TEXT COMMENT '漏洞描述',
    payload TEXT COMMENT '攻击载荷',
    solution TEXT COMMENT '解决方案',
    reference TEXT COMMENT '参考链接',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_scan_id (scan_id),
    INDEX idx_tool_name (tool_name),
    INDEX idx_severity (severity),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (scan_id) REFERENCES vulnerability_scans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扫描结果表';

-- ======================================
-- 代理相关表
-- ======================================

-- 代理表
CREATE TABLE IF NOT EXISTS agents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_id VARCHAR(255) NOT NULL UNIQUE COMMENT '代理唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '代理名称',
    description TEXT COMMENT '代理描述',
    hostname VARCHAR(255) NOT NULL COMMENT '主机名',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    platform ENUM('WINDOWS', 'LINUX') COMMENT '平台类型',
    os_version VARCHAR(100) COMMENT '操作系统版本',
    agent_version VARCHAR(50) COMMENT '代理版本',
    status ENUM('ONLINE', 'OFFLINE', 'ERROR', 'MAINTENANCE') DEFAULT 'OFFLINE' COMMENT '状态',
    registered_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_heartbeat DATETIME COMMENT '最后心跳时间',
    capabilities JSON COMMENT '能力描述',
    configuration JSON COMMENT '配置信息',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_agent_id (agent_id),
    INDEX idx_hostname (hostname),
    INDEX idx_ip_address (ip_address),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理表';

-- 基线任务表
CREATE TABLE IF NOT EXISTS baseline_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '任务描述',
    target_ip VARCHAR(45) NOT NULL COMMENT '目标IP',
    target_port INT COMMENT '目标端口',
    baseline_type VARCHAR(50) NOT NULL COMMENT '基线类型',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态',
    agent_id BIGINT COMMENT '执行代理ID',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_by BIGINT COMMENT '创建人ID',
    error_message TEXT COMMENT '错误信息',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_target_ip (target_ip),
    INDEX idx_baseline_type (baseline_type),
    INDEX idx_status (status),
    INDEX idx_agent_id (agent_id),
    INDEX idx_created_time (created_time),
    INDEX idx_deleted (deleted),
    CONSTRAINT fk_baseline_tasks_agent_id FOREIGN KEY (agent_id) REFERENCES agents(id),
    CONSTRAINT fk_baseline_tasks_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线任务表';

-- 基线结果表
CREATE TABLE IF NOT EXISTS baseline_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    rule_id VARCHAR(100) NOT NULL COMMENT '规则ID',
    rule_name VARCHAR(255) NOT NULL COMMENT '规则名称',
    rule_description TEXT COMMENT '规则描述',
    category VARCHAR(100) COMMENT '规则类别',
    severity VARCHAR(20) COMMENT '严重程度',
    status VARCHAR(20) NOT NULL COMMENT '检查状态',
    expected_value TEXT COMMENT '期望值',
    actual_value TEXT COMMENT '实际值',
    remediation TEXT COMMENT '修复建议',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_category (category),
    CONSTRAINT fk_baseline_results_task_id FOREIGN KEY (task_id) REFERENCES baseline_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线结果表';

-- 基线规则表
CREATE TABLE IF NOT EXISTS baseline_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id VARCHAR(100) NOT NULL UNIQUE COMMENT '规则ID',
    rule_name VARCHAR(255) NOT NULL COMMENT '规则名称',
    rule_description TEXT COMMENT '规则描述',
    baseline_type VARCHAR(50) NOT NULL COMMENT '基线类型',
    category VARCHAR(100) COMMENT '规则类别',
    severity ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO') NOT NULL COMMENT '严重程度',
    check_command TEXT NOT NULL COMMENT '检查命令',
    expected_value VARCHAR(500) COMMENT '期望值',
    comparison_operator VARCHAR(20) DEFAULT 'equals' COMMENT '比较操作符',
    remediation TEXT COMMENT '修复建议',
    reference VARCHAR(500) COMMENT '参考资料',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    timeout_seconds INT DEFAULT 30 COMMENT '超时时间(秒)',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_rule_id (rule_id),
    INDEX idx_baseline_type (baseline_type),
    INDEX idx_category (category),
    INDEX idx_severity (severity),
    INDEX idx_enabled (enabled),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基线规则表';

-- ======================================
-- 索引创建
-- ======================================

-- 基础索引
CREATE INDEX idx_vulnerabilities_status ON vulnerabilities(status);
CREATE INDEX idx_vulnerabilities_severity ON vulnerabilities(severity);
CREATE INDEX idx_vulnerabilities_assignee_id ON vulnerabilities(assignee_id);
CREATE INDEX idx_vulnerabilities_created_time ON vulnerabilities(created_time);
CREATE INDEX idx_vulnerabilities_asset_id ON vulnerabilities(asset_id);
CREATE INDEX idx_vulnerabilities_scan_task_id ON vulnerabilities(scan_task_id);
CREATE INDEX idx_vulnerabilities_source ON vulnerabilities(source);
CREATE INDEX idx_vulnerabilities_scan_engine ON vulnerabilities(scan_engine);
CREATE INDEX idx_vulnerabilities_plugin_id ON vulnerabilities(plugin_id);
CREATE INDEX idx_vulnerabilities_port ON vulnerabilities(port);
CREATE INDEX idx_vulnerabilities_protocol ON vulnerabilities(protocol);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);

-- ======================================
-- 初始数据插入
-- ======================================

-- 添加默认项目
INSERT IGNORE INTO projects (id, name, description, owner_id, status, type, priority, start_date, end_date, budget, tags, member_count, vulnerability_count, asset_count, progress, created_time, updated_time, deleted)
SELECT 1, '默认项目', '系统默认项目，用于存放未分类资产', 1, 'ACTIVE', 'General', 'MEDIUM', CURRENT_DATE(), DATE_ADD(CURRENT_DATE(), INTERVAL 1 YEAR), 0.00, 'default', 1, 0, 0, 0, NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE id = 1);

-- 插入默认资产检测配置
INSERT IGNORE INTO asset_detection_configs (name, description, detection_types, timeout_seconds, retry_count, concurrent_limit, include_fingerprint, is_default) VALUES
('默认配置', '系统默认的资产检测配置', '["PING", "TCP_PORT", "HTTP_SERVICE", "HTTPS_SERVICE"]', 30, 3, 10, TRUE, TRUE),
('快速检测', '快速检测配置，只进行基础连通性检测', '["PING", "TCP_PORT"]', 10, 1, 20, FALSE, FALSE),
('深度检测', '深度检测配置，包含所有检测类型和指纹识别', '["PING", "TCP_PORT", "HTTP_SERVICE", "HTTPS_SERVICE", "SSH_SERVICE", "DATABASE_SERVICE"]', 60, 5, 5, TRUE, FALSE);

-- 插入默认扫描配置
INSERT IGNORE INTO scan_configs (name, description, scan_engine, scan_type, scan_template, is_default, enabled, 
                                 timeout_minutes, max_concurrency, port_range, scan_depth, service_detection, 
                                 os_detection, vulnerability_scan, api_version, connection_timeout, read_timeout, ssl_verification) VALUES
('默认端口扫描', '使用Nmap进行基础端口扫描', 'NMAP', 'PORT_SCAN', 'QUICK_SCAN', TRUE, TRUE, 
 30, 10, '1-1000', 'NORMAL', TRUE, FALSE, FALSE, '1.0', 30, 300, TRUE),
 
('默认漏洞扫描', '使用内置引擎进行漏洞扫描', 'INTERNAL', 'SYSTEM_SCAN', 'FULL_SCAN', TRUE, TRUE, 
 120, 5, '1-65535', 'DEEP', TRUE, TRUE, TRUE, '1.0', 30, 300, TRUE),
 
('Web应用扫描', '专门用于Web应用的安全扫描', 'INTERNAL', 'WEB_SCAN', 'WEB_SCAN', FALSE, TRUE, 
 180, 3, '80,443,8080,8443', 'COMPREHENSIVE', TRUE, FALSE, TRUE, '1.0', 30, 300, TRUE),
 
('快速扫描', '快速端口和服务扫描', 'NMAP', 'PORT_SCAN', 'QUICK_SCAN', FALSE, TRUE, 
 15, 20, '1-1000', 'LIGHT', TRUE, FALSE, FALSE, '1.0', 30, 300, TRUE),
 
('全面扫描', '包含所有检测项的全面扫描', 'INTERNAL', 'COMPREHENSIVE_SCAN', 'FULL_SCAN', FALSE, TRUE, 
 300, 2, '1-65535', 'COMPREHENSIVE', TRUE, TRUE, TRUE, '1.0', 30, 300, TRUE);

-- 插入基线检查示例数据
INSERT IGNORE INTO baseline_checks (name, description, check_type, asset_id, created_by) VALUES
('系统安全基线检查', 'Linux系统安全配置基线检查', 'SYSTEM_SECURITY', 1, 1),
('网络安全基线检查', '网络服务和配置安全检查', 'NETWORK_SECURITY', 1, 1),
('Web应用安全基线检查', 'Web应用安全配置检查', 'WEB_SECURITY', 2, 1);

-- 插入基线检查项
INSERT IGNORE INTO baseline_check_items (baseline_check_id, item_code, item_name, description, category, severity, expected_value, check_command, remediation) VALUES
(1, 'SYS-001', '密码复杂度策略检查', '检查系统密码复杂度策略配置', '身份认证', 'HIGH', '密码长度>=8位，包含大小写字母、数字和特殊字符', 'cat /etc/pam.d/common-password | grep pam_pwquality', '配置/etc/pam.d/common-password文件，启用密码复杂度检查'),
(1, 'SYS-002', 'SSH安全配置检查', '检查SSH服务安全配置', '网络服务', 'HIGH', '禁用root登录，使用密钥认证', 'grep -E "^(PermitRootLogin|PasswordAuthentication)" /etc/ssh/sshd_config', '修改/etc/ssh/sshd_config，设置PermitRootLogin no，PasswordAuthentication no'),
(1, 'SYS-003', '防火墙状态检查', '检查系统防火墙是否启用', '网络安全', 'MEDIUM', '防火墙服务已启用并运行', 'systemctl is-active ufw || systemctl is-active firewalld', '启用并配置防火墙服务'),
(1, 'SYS-004', '系统更新状态检查', '检查系统是否有可用的安全更新', '系统维护', 'MEDIUM', '系统已安装最新安全更新', 'apt list --upgradable 2>/dev/null | grep -i security || yum check-update --security', '安装可用的安全更新'),
(1, 'SYS-005', '关键文件权限检查', '检查关键系统文件的权限设置', '文件系统', 'HIGH', '/etc/passwd权限为644，/etc/shadow权限为640', 'ls -l /etc/passwd /etc/shadow', '设置正确的文件权限：chmod 644 /etc/passwd; chmod 640 /etc/shadow');

-- 插入更多基线检查项
INSERT IGNORE INTO baseline_check_items (baseline_check_id, item_code, item_name, description, category, severity, expected_value, check_command, remediation) VALUES
(2, 'NET-001', '开放端口检查', '检查系统开放的网络端口', '网络服务', 'MEDIUM', '只开放必要的服务端口', 'netstat -tuln | grep LISTEN', '关闭不必要的网络服务和端口'),
(2, 'NET-002', '网络安全配置检查', '检查网络安全相关配置', '网络配置', 'MEDIUM', '禁用IP转发，启用SYN Cookies', 'sysctl net.ipv4.ip_forward net.ipv4.tcp_syncookies', '配置网络安全参数'),
(3, 'WEB-001', 'Web服务器安全配置检查', '检查Web服务器安全配置', 'Web安全', 'MEDIUM', '隐藏服务器版本信息，启用安全头', 'curl -I http://localhost', '配置Web服务器隐藏版本信息，添加安全响应头'),
(3, 'WEB-002', 'HTTPS配置检查', '检查HTTPS配置和证书', 'Web安全', 'HIGH', '启用HTTPS，使用有效证书', 'curl -I https://localhost', '配置HTTPS和有效的SSL证书'),
(3, 'WEB-003', 'Web应用防火墙检查', '检查Web应用防火墙配置', 'Web安全', 'MEDIUM', 'WAF已启用并正确配置', 'nginx -T | grep -i waf || apache2ctl -S | grep -i waf', '启用并配置Web应用防火墙');

-- 插入基线检查模板数据
INSERT IGNORE INTO baseline_check_templates (check_id, check_name, check_description, system_type, system_version, category, severity, check_command, expected_value, comparison_operator, remediation, reference) VALUES
-- Windows系统配置检查
('WIN_SYS_001', '检查系统版本', '验证Windows系统版本信息', 'WINDOWS', NULL, 'SYSTEM_CONFIGURATION', 'INFO', 'systeminfo | findstr /B /C:"OS Name"', NULL, 'contains', '确保系统版本符合要求', 'CIS Windows Benchmark'),
('WIN_SYS_002', '检查系统补丁状态', '检查系统是否安装了最新补丁', 'WINDOWS', NULL, 'SYSTEM_UPDATE', 'HIGH', 'wmic qfe list brief', NULL, 'contains', '及时安装系统补丁', 'Microsoft Security Updates'),
('WIN_SYS_003', '检查防火墙状态', '验证Windows防火墙是否启用', 'WINDOWS', NULL, 'FIREWALL_CONFIGURATION', 'HIGH', 'netsh advfirewall show allprofiles state', 'ON', 'contains', '启用Windows防火墙', 'CIS Windows Benchmark 9.1'),
-- Linux系统配置检查
('LNX_SYS_001', '检查系统版本', '验证Linux系统版本信息', 'LINUX', NULL, 'SYSTEM_CONFIGURATION', 'INFO', 'cat /etc/os-release | grep PRETTY_NAME', NULL, 'contains', '确保系统版本符合要求', 'CIS Linux Benchmark'),
('LNX_SYS_002', '检查内核版本', '检查Linux内核版本', 'LINUX', NULL, 'SYSTEM_CONFIGURATION', 'INFO', 'uname -r', NULL, 'contains', '保持内核版本更新', 'CIS Linux Benchmark'),
('LNX_SYS_003', '检查系统更新', '检查系统是否有可用更新', 'LINUX', NULL, 'SYSTEM_UPDATE', 'MEDIUM', 'which apt >/dev/null 2>&1 && apt list --upgradable 2>/dev/null | wc -l || echo "0"', '0', 'equals', '及时安装系统更新', 'CIS Linux Benchmark');

-- 插入示例漏洞数据
INSERT IGNORE INTO vulnerabilities (title, description, severity, status, category, reporter_id, assignee_id, discovered_date, cvss_score, risk_level, affected_systems, reproduction_steps, solution) VALUES
('SQL注入漏洞', '登录页面存在SQL注入漏洞，可能导致数据库信息泄露', 'HIGH', 'OPEN', 'Injection', 3, 3, '2024-01-10', 8.5, 'HIGH', 'Web应用登录模块', '1. 访问登录页面\n2. 在用户名字段输入: admin'' OR 1=1--\n3. 观察响应结果', '使用参数化查询或预编译语句'),
('跨站脚本攻击(XSS)', '用户评论功能存在存储型XSS漏洞', 'MEDIUM', 'IN_PROGRESS', 'XSS', 3, 2, '2024-01-12', 6.1, 'MEDIUM', '用户评论系统', '1. 登录系统\n2. 在评论框输入: <script>alert(''XSS'')</script>\n3. 提交评论并刷新页面', '对用户输入进行HTML编码和过滤'),
('弱密码策略', '系统密码策略过于宽松，允许弱密码', 'LOW', 'OPEN', 'Authentication', 2, NULL, '2024-01-15', 3.1, 'LOW', '用户认证系统', '1. 注册新用户\n2. 设置密码为: 123456\n3. 系统接受该密码', '实施强密码策略，要求密码复杂度'),
('敏感信息泄露', '错误页面暴露了系统版本和路径信息', 'MEDIUM', 'RESOLVED', 'Information Disclosure', 3, 3, '2024-01-20', 5.3, 'MEDIUM', '错误处理模块', '1. 访问不存在的页面\n2. 观察错误信息', '自定义错误页面，隐藏敏感信息'),
('文件上传漏洞', '文件上传功能未验证文件类型，可上传恶意文件', 'CRITICAL', 'OPEN', 'Upload', 3, 2, '2024-01-25', 9.8, 'CRITICAL', '文件上传模块', '1. 访问文件上传页面\n2. 上传.php文件\n3. 访问上传的文件路径', '验证文件类型和内容，限制上传目录权限');

-- 插入漏洞评论
INSERT IGNORE INTO vulnerability_comments (vulnerability_id, user_id, content, comment_type) VALUES 
(1, 2, '已确认该漏洞，优先级设为高', 'COMMENT'),
(1, 3, '正在分析修复方案', 'COMMENT'),
(2, 2, '漏洞已分配给开发团队', 'ASSIGNMENT'),
(4, 3, '漏洞已修复，请进行验证', 'STATUS_CHANGE'),
(5, 1, '这是一个严重的安全漏洞，需要立即处理', 'COMMENT');

-- 插入通知
INSERT IGNORE INTO notifications (user_id, title, content, type, status, related_id) VALUES
(2, '新漏洞分配', '您被分配了一个新的高危漏洞：SQL注入漏洞', 'VULNERABILITY', 'UNREAD', 1),
(3, '漏洞状态更新', '漏洞"敏感信息泄露"已被标记为已解决', 'VULNERABILITY', 'read', 4),
(4, '系统维护通知', '系统将于今晚22:00-24:00进行维护', 'SYSTEM', 'UNREAD', NULL);

-- 插入示例资产数据
INSERT IGNORE INTO assets (name, description, type, status, ip_address, domain, port, protocol, service, version, operating_system, importance, owner_id, location, vendor, tags, vulnerability_count, risk_score, notes) VALUES
('主Web服务器', '公司主要Web应用服务器', 'SERVER', 'ACTIVE', '192.168.1.10', 'www.example.com', 80, 'HTTP', 'Apache', '2.4.41', 'Ubuntu 20.04', 'CRITICAL', 1, '数据中心A机房', 'Dell', 'web,production,critical', 3, 7.5, '生产环境主服务器，需要重点关注'),
('数据库服务器', 'MySQL主数据库服务器', 'DATABASE', 'ACTIVE', '192.168.1.20', 'db.example.com', 3306, 'TCP', 'MySQL', '8.0.25', 'CentOS 8', 'CRITICAL', 1, '数据中心A机房', 'HP', 'database,production,critical', 1, 6.8, '存储核心业务数据'),
('负载均衡器', 'Nginx负载均衡器', 'NETWORK_DEVICE', 'ACTIVE', '192.168.1.5', 'lb.example.com', 443, 'HTTPS', 'Nginx', '1.18.0', 'Ubuntu 20.04', 'HIGH', 2, '数据中心A机房', 'F5', 'loadbalancer,network,production', 0, 4.2, '流量分发设备'),
('开发服务器', '开发环境Web服务器', 'SERVER', 'ACTIVE', '192.168.2.10', 'dev.example.com', 8080, 'HTTP', 'Tomcat', '9.0.50', 'Ubuntu 18.04', 'MEDIUM', 2, '开发区域', 'Dell', 'development,testing', 1, 3.5, '开发测试环境'),
('移动API服务器', '移动应用后端API服务器', 'SERVER', 'ACTIVE', '192.168.1.30', 'api.example.com', 8443, 'HTTPS', 'Node.js', '14.17.0', 'Ubuntu 20.04', 'HIGH', 2, '数据中心B机房', 'Dell', 'api,mobile,production', 0, 5.1, '移动应用后端服务'),
('防火墙设备', '边界防火墙', 'NETWORK_DEVICE', 'ACTIVE', '192.168.1.1', NULL, NULL, NULL, 'Firewall', 'v7.2', 'FortiOS', 'CRITICAL', 1, '网络边界', 'Fortinet', 'firewall,security,network', 0, 2.8, '网络安全边界设备'),
('文件服务器', 'NAS文件存储服务器', 'SERVER', 'ACTIVE', '192.168.1.40', 'files.example.com', 445, 'SMB', 'Samba', '4.11.6', 'Ubuntu 20.04', 'MEDIUM', 1, '数据中心A机房', 'Synology', 'storage,files,backup', 0, 4.0, '文件共享和备份'),
('监控服务器', '系统监控和日志服务器', 'SERVER', 'ACTIVE', '192.168.1.50', 'monitor.example.com', 3000, 'HTTP', 'Grafana', '8.1.2', 'CentOS 8', 'HIGH', 1, '数据中心B机房', 'HP', 'monitoring,logging,ops', 0, 3.2, '系统监控和运维');

-- 插入示例检测数据
INSERT IGNORE INTO asset_detections (asset_id, type, status, result, target, port, response_time, details, start_time, created_time) VALUES
(1, 'PING', 'COMPLETED', 'ONLINE', '192.168.1.10', NULL, 10, 'PING成功，响应时间: 10ms', NOW(), NOW()),
(1, 'TCP_PORT', 'COMPLETED', 'ONLINE', '192.168.1.10', 80, 50, '端口 80 开放，响应时间: 50ms', NOW(), NOW()),
(1, 'HTTP_SERVICE', 'COMPLETED', 'ONLINE', '192.168.1.10', 80, 100, 'HTTP服务正常，状态码: 200，响应时间: 100ms', NOW(), NOW());

-- 插入示例指纹数据
INSERT IGNORE INTO asset_fingerprints (asset_id, type, name, version, vendor, confidence, method, signature, port, protocol, active) VALUES
(1, 'WEB_SERVER', 'Apache HTTP Server', '2.4.41', 'Apache Software Foundation', 90, 'HTTP_HEADER', 'Server: Apache/2.4.41', 80, 'HTTP', TRUE),
(1, 'PROGRAMMING_LANGUAGE', 'PHP', '7.4.3', 'PHP Group', 85, 'HTTP_HEADER', 'X-Powered-By: PHP/7.4.3', 80, 'HTTP', TRUE),
(1, 'OPERATING_SYSTEM', 'Ubuntu Linux', '20.04', 'Canonical', 75, 'SERVER_BANNER', 'Ubuntu 20.04 LTS', NULL, NULL, TRUE);

-- 注意：用户数据通过 DataInitializer 类自动创建
-- 默认登录信息：admin/password123

-- 为现有agents记录设置agent_id值
UPDATE agents SET agent_id = CONCAT('agent-', id) WHERE agent_id IS NULL;