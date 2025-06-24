-- 添加projects表缺失的字段（忽略错误如果字段已存在）
SET sql_mode = '';

ALTER TABLE projects ADD COLUMN type VARCHAR(50) COMMENT '项目类型';
ALTER TABLE projects ADD COLUMN budget DECIMAL(15,2) COMMENT '预算';
ALTER TABLE projects ADD COLUMN tags VARCHAR(500) COMMENT '项目标签';
ALTER TABLE projects ADD COLUMN member_count INT DEFAULT 0 COMMENT '项目成员数量';
ALTER TABLE projects ADD COLUMN vulnerability_count INT DEFAULT 0 COMMENT '漏洞数量';
ALTER TABLE projects ADD COLUMN asset_count INT DEFAULT 0 COMMENT '资产数量';
ALTER TABLE projects ADD COLUMN progress INT DEFAULT 0 COMMENT '项目进度百分比';

-- 更新projects表的status枚举值
ALTER TABLE projects MODIFY COLUMN status ENUM('ACTIVE', 'INACTIVE', 'COMPLETED', 'ARCHIVED', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE';

-- 插入示例项目数据（如果不存在）
INSERT IGNORE INTO projects (id, name, description, owner_id, status, type, priority, start_date, end_date, budget, tags, member_count, vulnerability_count, asset_count, progress, created_time, updated_time, deleted) VALUES
(4, 'Web应用安全测试项目', '对公司主要Web应用进行全面的安全测试和漏洞评估', 1, 'ACTIVE', 'Security Testing', 'HIGH', '2024-01-01', '2024-06-30', 500000.00, 'web,security,testing', 5, 3, 3, 75, NOW(), NOW(), false),
(5, '移动应用安全评估', '对移动应用进行安全代码审计和渗透测试', 2, 'ACTIVE', 'Mobile Security', 'MEDIUM', '2024-02-01', '2024-08-31', 300000.00, 'mobile,security,audit', 3, 1, 2, 45, NOW(), NOW(), false),
(6, '网络基础设施安全加固', '对网络设备和服务器进行安全配置和漏洞修复', 1, 'ACTIVE', 'Infrastructure', 'CRITICAL', '2024-01-15', '2024-12-31', 800000.00, 'network,infrastructure,hardening', 8, 1, 3, 60, NOW(), NOW(), false),
(7, '云安全合规项目', '确保云环境符合安全合规要求', 1, 'COMPLETED', 'Cloud Security', 'HIGH', '2023-09-01', '2024-03-31', 400000.00, 'cloud,compliance,security', 4, 0, 0, 100, NOW(), NOW(), false);

-- 更新现有项目的新字段
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
    END
WHERE type IS NULL OR budget IS NULL;

-- 插入示例资产数据（如果assets表存在且为空）
INSERT IGNORE INTO assets (name, description, type, status, ip_address, domain, port, protocol, service, version, operating_system, importance, project_id, owner_id, location, vendor, tags, last_scan_time, vulnerability_count, risk_score, notes) VALUES
('主Web服务器', '公司主要Web应用服务器', 'SERVER', 'ACTIVE', '192.168.1.10', 'www.example.com', 80, 'HTTP', 'Apache', '2.4.41', 'Ubuntu 20.04', 'CRITICAL', 1, 1, '数据中心A机房', 'Dell', 'web,production,critical', '2024-01-20 10:00:00', 3, 7.5, '生产环境主服务器，需要重点关注'),
('数据库服务器', 'MySQL主数据库服务器', 'DATABASE', 'ACTIVE', '192.168.1.20', 'db.example.com', 3306, 'TCP', 'MySQL', '8.0.25', 'CentOS 8', 'CRITICAL', 1, 1, '数据中心A机房', 'HP', 'database,production,critical', '2024-01-18 14:30:00', 1, 6.8, '存储核心业务数据'),
('负载均衡器', 'Nginx负载均衡器', 'NETWORK_DEVICE', 'ACTIVE', '192.168.1.5', 'lb.example.com', 443, 'HTTPS', 'Nginx', '1.18.0', 'Ubuntu 20.04', 'HIGH', 1, 2, '数据中心A机房', 'F5', 'loadbalancer,network,production', '2024-01-22 09:15:00', 0, 4.2, '流量分发设备'),
('开发服务器', '开发环境Web服务器', 'SERVER', 'ACTIVE', '192.168.2.10', 'dev.example.com', 8080, 'HTTP', 'Tomcat', '9.0.50', 'Ubuntu 18.04', 'MEDIUM', 2, 2, '开发区域', 'Dell', 'development,testing', '2024-01-15 16:45:00', 1, 3.5, '开发测试环境'),
('移动API服务器', '移动应用后端API服务器', 'SERVER', 'ACTIVE', '192.168.1.30', 'api.example.com', 8443, 'HTTPS', 'Node.js', '14.17.0', 'Ubuntu 20.04', 'HIGH', 2, 2, '数据中心B机房', 'Dell', 'api,mobile,production', '2024-01-19 11:20:00', 0, 5.1, '移动应用后端服务'),
('防火墙设备', '边界防火墙', 'NETWORK_DEVICE', 'ACTIVE', '192.168.1.1', NULL, NULL, NULL, 'Firewall', 'v7.2', 'FortiOS', 'CRITICAL', 3, 1, '网络边界', 'Fortinet', 'firewall,security,network', '2024-01-21 08:00:00', 0, 2.8, '网络安全边界设备'),
('文件服务器', 'NAS文件存储服务器', 'SERVER', 'ACTIVE', '192.168.1.40', 'files.example.com', 445, 'SMB', 'Samba', '4.11.6', 'Ubuntu 20.04', 'MEDIUM', 3, 1, '数据中心A机房', 'Synology', 'storage,files,backup', '2024-01-17 13:10:00', 0, 4.0, '文件共享和备份'),
('监控服务器', '系统监控和日志服务器', 'SERVER', 'ACTIVE', '192.168.1.50', 'monitor.example.com', 3000, 'HTTP', 'Grafana', '8.1.2', 'CentOS 8', 'HIGH', 3, 1, '数据中心B机房', 'HP', 'monitoring,logging,ops', '2024-01-23 07:30:00', 0, 3.2, '系统监控和运维');
