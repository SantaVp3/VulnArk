-- VulnArk 漏洞管理平台数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS vulnark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vulnark;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    department VARCHAR(100) COMMENT '部门',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    two_factor_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用双因素认证',
    two_factor_secret VARCHAR(255) DEFAULT '' COMMENT '双因素认证密钥',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role_id (role_id)
) COMMENT '用户表';

-- 角色表
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    permissions JSON COMMENT '权限列表',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '角色表';

-- 资产表
CREATE TABLE assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '资产名称',
    type VARCHAR(50) NOT NULL COMMENT '资产类型：server,database,application,network',
    category VARCHAR(50) COMMENT '资产分类',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    domain VARCHAR(255) COMMENT '域名',
    port VARCHAR(20) COMMENT '端口',
    os VARCHAR(100) COMMENT '操作系统',
    version VARCHAR(50) COMMENT '版本信息',
    department VARCHAR(100) COMMENT '所属部门',
    owner_id BIGINT COMMENT '负责人ID',
    business_line VARCHAR(100) COMMENT '业务线',
    importance_level TINYINT DEFAULT 3 COMMENT '重要性等级：1-高，2-中，3-低',
    description TEXT COMMENT '资产描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-下线',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_owner_id (owner_id),
    INDEX idx_department (department)
) COMMENT '资产表';

-- OWASP Top10 分类表
CREATE TABLE owasp_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '分类代码',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    year INT DEFAULT 2021 COMMENT '年份版本',
    severity_level TINYINT DEFAULT 3 COMMENT '严重程度：1-高，2-中，3-低',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT 'OWASP Top10 分类表';

-- 漏洞表
CREATE TABLE vulnerabilities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '漏洞标题',
    description TEXT COMMENT '漏洞描述',
    cve_id VARCHAR(50) COMMENT 'CVE编号',
    cnvd_id VARCHAR(50) COMMENT 'CNVD编号',
    owasp_category_id BIGINT COMMENT 'OWASP分类ID',
    severity_level TINYINT NOT NULL COMMENT '严重程度：1-严重，2-高危，3-中危，4-低危',
    cvss_score DECIMAL(3,1) COMMENT 'CVSS评分',
    asset_id BIGINT NOT NULL COMMENT '关联资产ID',
    discoverer_id BIGINT COMMENT '发现人ID',
    assignee_id BIGINT COMMENT '处理人ID',
    status VARCHAR(20) DEFAULT 'new' COMMENT '状态：new,processing,fixed,verified,closed',
    impact_scope TEXT COMMENT '影响范围',
    reproduction_steps TEXT COMMENT '复现步骤',
    fix_suggestion TEXT COMMENT '修复建议',
    discovered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
    assigned_at TIMESTAMP NULL COMMENT '分配时间',
    fixed_at TIMESTAMP NULL COMMENT '修复时间',
    verified_at TIMESTAMP NULL COMMENT '验证时间',
    closed_at TIMESTAMP NULL COMMENT '关闭时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_asset_id (asset_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity_level),
    INDEX idx_owasp_category (owasp_category_id)
) COMMENT '漏洞表';

-- 漏洞附件表
CREATE TABLE vulnerability_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_id BIGINT NOT NULL COMMENT '漏洞ID',
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) COMMENT '文件类型',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vulnerability_id (vulnerability_id)
) COMMENT '漏洞附件表';

-- 漏洞分配记录表
CREATE TABLE vulnerability_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_id BIGINT NOT NULL COMMENT '漏洞ID',
    assignee_id BIGINT NOT NULL COMMENT '被分配人ID',
    assigner_id BIGINT NOT NULL COMMENT '分配人ID',
    assignment_type VARCHAR(20) DEFAULT 'manual' COMMENT '分配类型：manual,auto',
    assignment_reason TEXT COMMENT '分配原因',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vulnerability_id (vulnerability_id),
    INDEX idx_assignee_id (assignee_id)
) COMMENT '漏洞分配记录表';

-- 分配规则表
CREATE TABLE assignment_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型：asset_type,severity,department',
    conditions JSON NOT NULL COMMENT '规则条件',
    assignee_id BIGINT NOT NULL COMMENT '分配给的用户ID',
    priority INT DEFAULT 0 COMMENT '优先级',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '分配规则表';

-- 报告表
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_id BIGINT NOT NULL COMMENT '关联漏洞ID',
    title VARCHAR(255) NOT NULL COMMENT '报告标题',
    type VARCHAR(50) NOT NULL COMMENT '报告类型：fix,verify,analysis',
    content TEXT COMMENT '报告内容',
    file_path VARCHAR(500) COMMENT '报告文件路径',
    file_name VARCHAR(255) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft,submitted,approved,rejected',
    submitted_at TIMESTAMP NULL COMMENT '提交时间',
    approved_at TIMESTAMP NULL COMMENT '审批时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vulnerability_id (vulnerability_id),
    INDEX idx_author_id (author_id),
    INDEX idx_type (type)
) COMMENT '报告表';

-- 知识库表
CREATE TABLE knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    category VARCHAR(100) COMMENT '分类',
    tags JSON COMMENT '标签',
    vulnerability_type VARCHAR(100) COMMENT '漏洞类型',
    fix_template TEXT COMMENT '修复模板',
    reference_links JSON COMMENT '参考链接',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    is_public TINYINT DEFAULT 1 COMMENT '是否公开',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_vulnerability_type (vulnerability_type),
    FULLTEXT idx_content (title, content)
) COMMENT '知识库表';

-- 通知表
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(255) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type VARCHAR(50) NOT NULL COMMENT '通知类型：system,email,assignment,reminder',
    related_type VARCHAR(50) COMMENT '关联类型：vulnerability,report,user',
    related_id BIGINT COMMENT '关联ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read)
) COMMENT '通知表';

-- AI对话记录表
CREATE TABLE ai_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    message TEXT NOT NULL COMMENT '用户消息',
    response TEXT NOT NULL COMMENT 'AI回复',
    context_type VARCHAR(50) COMMENT '上下文类型：vulnerability,knowledge,general',
    context_id BIGINT COMMENT '上下文ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id)
) COMMENT 'AI对话记录表';

-- 操作日志表
CREATE TABLE operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    action VARCHAR(100) NOT NULL COMMENT '操作动作',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    details JSON COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_created_at (created_at)
) COMMENT '操作日志表';

-- 添加基础外键约束（只添加基础表之间的约束）
ALTER TABLE users ADD CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id);
ALTER TABLE assets ADD CONSTRAINT fk_assets_owner_id FOREIGN KEY (owner_id) REFERENCES users(id);
ALTER TABLE vulnerabilities ADD CONSTRAINT fk_vulnerabilities_asset_id FOREIGN KEY (asset_id) REFERENCES assets(id);
ALTER TABLE vulnerabilities ADD CONSTRAINT fk_vulnerabilities_discoverer_id FOREIGN KEY (discoverer_id) REFERENCES users(id);
ALTER TABLE vulnerabilities ADD CONSTRAINT fk_vulnerabilities_assignee_id FOREIGN KEY (assignee_id) REFERENCES users(id);
