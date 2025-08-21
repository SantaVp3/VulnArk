-- 添加权限系统相关表

USE vulnark;

-- 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '权限名称',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    description TEXT COMMENT '权限描述',
    module VARCHAR(50) NOT NULL COMMENT '模块名称',
    action VARCHAR(50) NOT NULL COMMENT '操作动作',
    resource VARCHAR(50) NOT NULL COMMENT '资源类型',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    INDEX idx_name (name),
    INDEX idx_module (module),
    INDEX idx_action (action),
    INDEX idx_resource (resource),
    INDEX idx_is_active (is_active),
    INDEX idx_module_action_resource (module, action, resource)
) COMMENT '权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) COMMENT '角色权限关联表';

-- 用户角色关联表（如果不存在）
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) COMMENT '用户角色关联表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    value JSON NOT NULL COMMENT '配置值',
    type VARCHAR(20) NOT NULL COMMENT '配置类型',
    category VARCHAR(50) NOT NULL COMMENT '配置分类',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    description TEXT COMMENT '配置描述',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    is_editable BOOLEAN DEFAULT TRUE COMMENT '是否可编辑',
    validation TEXT COMMENT '验证规则',
    default_value JSON COMMENT '默认值',
    INDEX idx_key (`key`),
    INDEX idx_category (category),
    INDEX idx_is_public (is_public),
    INDEX idx_is_editable (is_editable)
) COMMENT '系统配置表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    action VARCHAR(100) NOT NULL COMMENT '操作动作',
    resource VARCHAR(100) NOT NULL COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    method VARCHAR(10) COMMENT 'HTTP方法',
    path VARCHAR(255) COMMENT '请求路径',
    ip VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    request TEXT COMMENT '请求内容',
    response TEXT COMMENT '响应内容',
    status INT COMMENT '响应状态码',
    duration BIGINT COMMENT '处理时长(毫秒)',
    error TEXT COMMENT '错误信息',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_action (action),
    INDEX idx_resource (resource),
    INDEX idx_method (method),
    INDEX idx_status (status),
    INDEX idx_ip (ip),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT '审计日志表';

-- 插入默认权限
INSERT INTO permissions (name, display_name, description, module, action, resource) VALUES
-- 用户管理权限
('user:create', '创建用户', '创建新用户账号', 'user', 'create', 'user'),
('user:read', '查看用户', '查看用户信息', 'user', 'read', 'user'),
('user:update', '更新用户', '更新用户信息', 'user', 'update', 'user'),
('user:delete', '删除用户', '删除用户账号', 'user', 'delete', 'user'),
('user:manage', '管理用户', '完整的用户管理权限', 'user', 'manage', 'user'),

-- 角色管理权限
('role:create', '创建角色', '创建新角色', 'role', 'create', 'role'),
('role:read', '查看角色', '查看角色信息', 'role', 'read', 'role'),
('role:update', '更新角色', '更新角色信息', 'role', 'update', 'role'),
('role:delete', '删除角色', '删除角色', 'role', 'delete', 'role'),
('role:assign', '分配角色', '为用户分配角色', 'role', 'assign', 'role'),

-- 资产管理权限
('asset:create', '创建资产', '创建新资产', 'asset', 'create', 'asset'),
('asset:read', '查看资产', '查看资产信息', 'asset', 'read', 'asset'),
('asset:update', '更新资产', '更新资产信息', 'asset', 'update', 'asset'),
('asset:delete', '删除资产', '删除资产', 'asset', 'delete', 'asset'),
('asset:import', '导入资产', '批量导入资产', 'asset', 'import', 'asset'),
('asset:export', '导出资产', '导出资产数据', 'asset', 'export', 'asset'),

-- 漏洞管理权限
('vulnerability:create', '创建漏洞', '创建新漏洞', 'vulnerability', 'create', 'vulnerability'),
('vulnerability:read', '查看漏洞', '查看漏洞信息', 'vulnerability', 'read', 'vulnerability'),
('vulnerability:update', '更新漏洞', '更新漏洞信息', 'vulnerability', 'update', 'vulnerability'),
('vulnerability:delete', '删除漏洞', '删除漏洞', 'vulnerability', 'delete', 'vulnerability'),
('vulnerability:assign', '分配漏洞', '分配漏洞处理人', 'vulnerability', 'assign', 'vulnerability'),

-- 报告管理权限
('report:create', '创建报告', '创建新报告', 'report', 'create', 'report'),
('report:read', '查看报告', '查看报告内容', 'report', 'read', 'report'),
('report:update', '更新报告', '更新报告信息', 'report', 'update', 'report'),
('report:delete', '删除报告', '删除报告', 'report', 'delete', 'report'),
('report:review', '审核报告', '审核报告内容', 'report', 'review', 'report'),

-- 通知管理权限
('notification:create', '创建通知', '创建系统通知', 'notification', 'create', 'notification'),
('notification:read', '查看通知', '查看通知信息', 'notification', 'read', 'notification'),
('notification:manage', '管理通知', '完整的通知管理权限', 'notification', 'manage', 'notification'),

-- 系统管理权限
('system:config', '系统配置', '管理系统配置', 'system', 'manage', 'config'),
('system:audit', '审计日志', '查看审计日志', 'system', 'read', 'audit'),
('system:permission', '权限管理', '管理系统权限', 'system', 'manage', 'permission');

-- 插入默认系统配置
INSERT INTO system_configs (`key`, value, type, category, display_name, description, is_public, is_editable, default_value) VALUES
('system.name', '{"value": "VulnArk漏洞管理系统"}', 'string', 'system', '系统名称', '系统显示名称', TRUE, TRUE, '{"value": "VulnArk漏洞管理系统"}'),
('system.version', '{"value": "1.0.0"}', 'string', 'system', '系统版本', '当前系统版本号', TRUE, FALSE, '{"value": "1.0.0"}'),
('system.description', '{"value": "专业的漏洞管理和安全评估平台"}', 'string', 'system', '系统描述', '系统功能描述', TRUE, TRUE, '{"value": "专业的漏洞管理和安全评估平台"}'),
('security.password_min_length', '{"value": 8}', 'int', 'security', '密码最小长度', '用户密码最小长度要求', FALSE, TRUE, '{"value": 8}'),
('security.session_timeout', '{"value": 3600}', 'int', 'security', '会话超时时间', '用户会话超时时间（秒）', FALSE, TRUE, '{"value": 3600}'),
('security.max_login_attempts', '{"value": 5}', 'int', 'security', '最大登录尝试次数', '账号锁定前的最大登录失败次数', FALSE, TRUE, '{"value": 5}'),
('notification.email_enabled', '{"value": true}', 'bool', 'notification', '邮件通知开关', '是否启用邮件通知功能', FALSE, TRUE, '{"value": true}'),
('notification.system_enabled', '{"value": true}', 'bool', 'notification', '系统通知开关', '是否启用系统内通知', FALSE, TRUE, '{"value": true}'),
('audit.log_retention_days', '{"value": 90}', 'int', 'audit', '审计日志保留天数', '审计日志保留天数，超过将自动删除', FALSE, TRUE, '{"value": 90}'),
('audit.log_enabled', '{"value": true}', 'bool', 'audit', '审计日志开关', '是否启用审计日志记录', FALSE, TRUE, '{"value": true}');

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions WHERE is_active = TRUE;

-- 为现有用户分配管理员角色（如果还没有角色）
INSERT INTO user_roles (user_id, role_id)
SELECT id, 1 FROM users 
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = users.id
);
