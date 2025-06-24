-- 初始化数据

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO users (username, email, password, full_name, role, status) VALUES 
('admin', 'admin@vulnark.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkNvAWGG', '系统管理员', 'ADMIN', 'ACTIVE'),
('manager', 'manager@vulnark.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkNvAWGG', '项目经理', 'MANAGER', 'ACTIVE'),
('analyst', 'analyst@vulnark.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkNvAWGG', '安全分析师', 'ANALYST', 'ACTIVE'),
('viewer', 'viewer@vulnark.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkNvAWGG', '查看者', 'VIEWER', 'ACTIVE');

-- 插入示例项目
INSERT INTO projects (name, description, owner_id, status, priority, start_date) VALUES 
('Web应用安全测试', 'Web应用程序安全漏洞扫描与测试项目', 1, 'ACTIVE', 'HIGH', '2024-01-01'),
('移动应用安全评估', '移动应用程序安全评估与漏洞分析', 2, 'ACTIVE', 'MEDIUM', '2024-01-15'),
('网络基础设施安全', '网络设备和基础设施安全检查', 1, 'ACTIVE', 'CRITICAL', '2024-02-01');

-- 插入项目成员
INSERT INTO project_members (project_id, user_id, role) VALUES 
(1, 1, 'OWNER'),
(1, 2, 'MANAGER'),
(1, 3, 'MEMBER'),
(2, 2, 'OWNER'),
(2, 3, 'MEMBER'),
(2, 4, 'VIEWER'),
(3, 1, 'OWNER'),
(3, 3, 'MEMBER');

-- 插入示例漏洞
INSERT INTO vulnerabilities (title, description, severity, status, category, project_id, reporter_id, assignee_id, discovered_date, cvss_score, risk_level, affected_systems, reproduction_steps, solution) VALUES 
('SQL注入漏洞', '登录页面存在SQL注入漏洞，可能导致数据库信息泄露', 'HIGH', 'OPEN', 'Injection', 1, 3, 3, '2024-01-10', 8.5, 'HIGH', 'Web应用登录模块', '1. 访问登录页面\n2. 在用户名字段输入: admin'' OR 1=1--\n3. 观察响应结果', '使用参数化查询或预编译语句'),
('跨站脚本攻击(XSS)', '用户评论功能存在存储型XSS漏洞', 'MEDIUM', 'IN_PROGRESS', 'XSS', 1, 3, 2, '2024-01-12', 6.1, 'MEDIUM', '用户评论系统', '1. 登录系统\n2. 在评论框输入: <script>alert(''XSS'')</script>\n3. 提交评论并刷新页面', '对用户输入进行HTML编码和过滤'),
('弱密码策略', '系统密码策略过于宽松，允许弱密码', 'LOW', 'OPEN', 'Authentication', 2, 2, NULL, '2024-01-15', 3.1, 'LOW', '用户认证系统', '1. 注册新用户\n2. 设置密码为: 123456\n3. 系统接受该密码', '实施强密码策略，要求密码复杂度'),
('敏感信息泄露', '错误页面暴露了系统版本和路径信息', 'MEDIUM', 'RESOLVED', 'Information Disclosure', 3, 3, 3, '2024-01-20', 5.3, 'MEDIUM', '错误处理模块', '1. 访问不存在的页面\n2. 观察错误信息', '自定义错误页面，隐藏敏感信息'),
('文件上传漏洞', '文件上传功能未验证文件类型，可上传恶意文件', 'CRITICAL', 'OPEN', 'Upload', 1, 3, 2, '2024-01-25', 9.8, 'CRITICAL', '文件上传模块', '1. 访问文件上传页面\n2. 上传.php文件\n3. 访问上传的文件路径', '验证文件类型和内容，限制上传目录权限');

-- 插入漏洞评论
INSERT INTO vulnerability_comments (vulnerability_id, user_id, content, comment_type) VALUES 
(1, 2, '已确认该漏洞，优先级设为高', 'COMMENT'),
(1, 3, '正在分析修复方案', 'COMMENT'),
(2, 2, '漏洞已分配给开发团队', 'ASSIGNMENT'),
(4, 3, '漏洞已修复，请进行验证', 'STATUS_CHANGE'),
(5, 1, '这是一个严重的安全漏洞，需要立即处理', 'COMMENT');

-- 插入通知
INSERT INTO notifications (user_id, title, content, type, status, related_id) VALUES
(2, '新漏洞分配', '您被分配了一个新的高危漏洞：SQL注入漏洞', 'VULNERABILITY', 'UNREAD', 1),
(3, '漏洞状态更新', '漏洞"敏感信息泄露"已被标记为已解决', 'VULNERABILITY', 'READ', 4),
(1, '项目成员变更', '用户analyst已加入项目"网络基础设施安全"', 'PROJECT', 'UNREAD', 3),
(4, '系统维护通知', '系统将于今晚22:00-24:00进行维护', 'SYSTEM', 'UNREAD', NULL);

-- 更新项目表数据，添加新字段
UPDATE projects SET
    type = CASE
        WHEN name = 'Web应用安全测试' THEN 'Security Testing'
        WHEN name = '移动应用安全评估' THEN 'Mobile Security'
        WHEN name = '网络基础设施安全' THEN 'Infrastructure'
        ELSE 'General'
    END,
    budget = CASE
        WHEN name = 'Web应用安全测试' THEN 500000.00
        WHEN name = '移动应用安全评估' THEN 300000.00
        WHEN name = '网络基础设施安全' THEN 800000.00
        ELSE 100000.00
    END,
    tags = CASE
        WHEN name = 'Web应用安全测试' THEN 'web,security,testing'
        WHEN name = '移动应用安全评估' THEN 'mobile,security,audit'
        WHEN name = '网络基础设施安全' THEN 'network,infrastructure,hardening'
        ELSE 'general'
    END,
    member_count = 3,
    vulnerability_count = CASE
        WHEN id = 1 THEN 3
        WHEN id = 2 THEN 1
        WHEN id = 3 THEN 1
        ELSE 0
    END,
    asset_count = CASE
        WHEN id = 1 THEN 3
        WHEN id = 2 THEN 2
        WHEN id = 3 THEN 3
        ELSE 0
    END,
    progress = CASE
        WHEN name = 'Web应用安全测试' THEN 75
        WHEN name = '移动应用安全评估' THEN 45
        WHEN name = '网络基础设施安全' THEN 60
        ELSE 0
    END;

-- 插入示例资产数据
INSERT INTO assets (name, description, type, status, ip_address, domain, port, protocol, service, version, operating_system, importance, project_id, owner_id, location, vendor, tags, last_scan_time, vulnerability_count, risk_score, notes) VALUES
('主Web服务器', '公司主要Web应用服务器', 'SERVER', 'ACTIVE', '192.168.1.10', 'www.example.com', 80, 'HTTP', 'Apache', '2.4.41', 'Ubuntu 20.04', 'CRITICAL', 1, 1, '数据中心A机房', 'Dell', 'web,production,critical', '2024-01-20 10:00:00', 3, 7.5, '生产环境主服务器，需要重点关注'),
('数据库服务器', 'MySQL主数据库服务器', 'DATABASE', 'ACTIVE', '192.168.1.20', 'db.example.com', 3306, 'TCP', 'MySQL', '8.0.25', 'CentOS 8', 'CRITICAL', 1, 1, '数据中心A机房', 'HP', 'database,production,critical', '2024-01-18 14:30:00', 1, 6.8, '存储核心业务数据'),
('负载均衡器', 'Nginx负载均衡器', 'NETWORK_DEVICE', 'ACTIVE', '192.168.1.5', 'lb.example.com', 443, 'HTTPS', 'Nginx', '1.18.0', 'Ubuntu 20.04', 'HIGH', 1, 2, '数据中心A机房', 'F5', 'loadbalancer,network,production', '2024-01-22 09:15:00', 0, 4.2, '流量分发设备'),
('开发服务器', '开发环境Web服务器', 'SERVER', 'ACTIVE', '192.168.2.10', 'dev.example.com', 8080, 'HTTP', 'Tomcat', '9.0.50', 'Ubuntu 18.04', 'MEDIUM', 2, 2, '开发区域', 'Dell', 'development,testing', '2024-01-15 16:45:00', 1, 3.5, '开发测试环境'),
('移动API服务器', '移动应用后端API服务器', 'SERVER', 'ACTIVE', '192.168.1.30', 'api.example.com', 8443, 'HTTPS', 'Node.js', '14.17.0', 'Ubuntu 20.04', 'HIGH', 2, 2, '数据中心B机房', 'Dell', 'api,mobile,production', '2024-01-19 11:20:00', 0, 5.1, '移动应用后端服务'),
('防火墙设备', '边界防火墙', 'NETWORK_DEVICE', 'ACTIVE', '192.168.1.1', NULL, NULL, NULL, 'Firewall', 'v7.2', 'FortiOS', 'CRITICAL', 3, 1, '网络边界', 'Fortinet', 'firewall,security,network', '2024-01-21 08:00:00', 0, 2.8, '网络安全边界设备'),
('文件服务器', 'NAS文件存储服务器', 'SERVER', 'ACTIVE', '192.168.1.40', 'files.example.com', 445, 'SMB', 'Samba', '4.11.6', 'Ubuntu 20.04', 'MEDIUM', 3, 1, '数据中心A机房', 'Synology', 'storage,files,backup', '2024-01-17 13:10:00', 0, 4.0, '文件共享和备份'),
('监控服务器', '系统监控和日志服务器', 'SERVER', 'ACTIVE', '192.168.1.50', 'monitor.example.com', 3000, 'HTTP', 'Grafana', '8.1.2', 'CentOS 8', 'HIGH', 3, 1, '数据中心B机房', 'HP', 'monitoring,logging,ops', '2024-01-23 07:30:00', 0, 3.2, '系统监控和运维');
