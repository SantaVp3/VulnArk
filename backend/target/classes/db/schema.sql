-- 创建数据库
CREATE DATABASE IF NOT EXISTS vulnark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vulnark;

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
    status ENUM('ACTIVE', 'INACTIVE', 'COMPLETED', 'ARCHIVED', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '项目状态',
    type VARCHAR(50) COMMENT '项目类型',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    budget DECIMAL(15,2) COMMENT '预算',
    tags VARCHAR(500) COMMENT '项目标签',
    member_count INT DEFAULT 0 COMMENT '项目成员数量',
    vulnerability_count INT DEFAULT 0 COMMENT '漏洞数量',
    asset_count INT DEFAULT 0 COMMENT '资产数量',
    progress INT DEFAULT 0 COMMENT '项目进度百分比',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 项目成员表
CREATE TABLE IF NOT EXISTS project_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('OWNER', 'MANAGER', 'MEMBER', 'VIEWER') NOT NULL DEFAULT 'MEMBER' COMMENT '项目角色',
    joined_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_project_user (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- 资产表
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '资产名称',
    description TEXT COMMENT '资产描述',
    type ENUM('SERVER', 'WORKSTATION', 'NETWORK_DEVICE', 'DATABASE', 'WEB_APPLICATION', 'MOBILE_APPLICATION', 'IOT_DEVICE', 'CLOUD_SERVICE', 'OTHER') NOT NULL COMMENT '资产类型',
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'DECOMMISSIONED') NOT NULL DEFAULT 'ACTIVE' COMMENT '资产状态',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    domain VARCHAR(255) COMMENT '域名',
    port INT COMMENT '端口',
    protocol VARCHAR(20) COMMENT '协议',
    service VARCHAR(100) COMMENT '服务',
    version VARCHAR(50) COMMENT '版本',
    operating_system VARCHAR(100) COMMENT '操作系统',
    importance ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM' COMMENT '重要性等级',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    owner_id BIGINT COMMENT '负责人ID',
    location VARCHAR(200) COMMENT '位置',
    vendor VARCHAR(100) COMMENT '供应商',
    tags VARCHAR(500) COMMENT '资产标签',
    last_scan_time DATETIME COMMENT '最后扫描时间',
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
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    reporter_id BIGINT NOT NULL COMMENT '报告人ID',
    assignee_id BIGINT COMMENT '负责人ID',
    discovered_date DATE NOT NULL COMMENT '发现日期',
    due_date DATE COMMENT '修复截止日期',
    resolved_date DATE COMMENT '解决日期',
    verification_status ENUM('PENDING', 'VERIFIED', 'FALSE_POSITIVE') DEFAULT 'PENDING' COMMENT '验证状态',
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') COMMENT '风险等级',
    affected_systems TEXT COMMENT '受影响系统',
    reproduction_steps TEXT COMMENT '复现步骤',
    solution TEXT COMMENT '解决方案',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    FOREIGN KEY (project_id) REFERENCES projects(id),
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

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type ENUM('VULNERABILITY', 'PROJECT', 'SYSTEM') NOT NULL COMMENT '通知类型',
    status ENUM('UNREAD', 'READ') NOT NULL DEFAULT 'UNREAD' COMMENT '状态',
    related_id BIGINT COMMENT '关联ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_time DATETIME COMMENT '阅读时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

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
    scan_type ENUM('PING_SWEEP', 'PORT_SCAN', 'SERVICE_DETECTION', 'FULL_SCAN') NOT NULL COMMENT '扫描类型',
    scan_ports VARCHAR(1000) COMMENT '扫描端口范围',
    scan_options JSON COMMENT '扫描选项配置',
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

-- 创建索引
CREATE INDEX idx_vulnerabilities_project_id ON vulnerabilities(project_id);
CREATE INDEX idx_vulnerabilities_status ON vulnerabilities(status);
CREATE INDEX idx_vulnerabilities_severity ON vulnerabilities(severity);
CREATE INDEX idx_vulnerabilities_assignee_id ON vulnerabilities(assignee_id);
CREATE INDEX idx_vulnerabilities_created_time ON vulnerabilities(created_time);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
