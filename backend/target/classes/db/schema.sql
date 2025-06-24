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

-- 创建索引
CREATE INDEX idx_vulnerabilities_project_id ON vulnerabilities(project_id);
CREATE INDEX idx_vulnerabilities_status ON vulnerabilities(status);
CREATE INDEX idx_vulnerabilities_severity ON vulnerabilities(severity);
CREATE INDEX idx_vulnerabilities_assignee_id ON vulnerabilities(assignee_id);
CREATE INDEX idx_vulnerabilities_created_time ON vulnerabilities(created_time);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
