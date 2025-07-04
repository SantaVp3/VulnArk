-- 创建扫描配置表
CREATE TABLE IF NOT EXISTS scan_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    description TEXT COMMENT '配置描述',
    scan_engine ENUM('NESSUS', 'OPENVAS', 'AWVS', 'NUCLEI', 'NMAP', 'INTERNAL') NOT NULL COMMENT '扫描引擎',
    scan_type ENUM('PORT_SCAN', 'WEB_SCAN', 'SYSTEM_SCAN', 'COMPREHENSIVE_SCAN', 'CUSTOM_SCAN') NOT NULL COMMENT '扫描类型',
    scan_template ENUM('QUICK_SCAN', 'BASIC_SCAN', 'FULL_SCAN', 'ADVANCED_SCAN', 'WEB_SCAN', 'API_SCAN', 'PORT_SCAN', 'COMPLIANCE', 'CUSTOM') COMMENT '扫描模板',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认配置',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    
    -- 扫描参数配置
    parameters TEXT COMMENT '扫描参数配置',
    timeout_minutes INT DEFAULT 60 COMMENT '超时时间（分钟）',
    max_concurrency INT DEFAULT 5 COMMENT '最大并发数',
    port_range VARCHAR(200) COMMENT '端口范围',
    exclude_ports VARCHAR(200) COMMENT '排除的端口',
    scan_depth ENUM('LIGHT', 'NORMAL', 'DEEP', 'COMPREHENSIVE') DEFAULT 'NORMAL' COMMENT '扫描深度',
    
    -- 扫描选项
    scan_udp BOOLEAN DEFAULT FALSE COMMENT '是否扫描UDP端口',
    service_detection BOOLEAN DEFAULT TRUE COMMENT '是否进行服务识别',
    os_detection BOOLEAN DEFAULT TRUE COMMENT '是否进行操作系统识别',
    script_scan BOOLEAN DEFAULT FALSE COMMENT '是否进行脚本扫描',
    vulnerability_scan BOOLEAN DEFAULT TRUE COMMENT '是否进行漏洞扫描',
    web_app_scan BOOLEAN DEFAULT FALSE COMMENT '是否进行Web应用扫描',
    
    -- 高级配置
    custom_scripts TEXT COMMENT '自定义扫描脚本',
    exclude_vuln_types TEXT COMMENT '排除的漏洞类型',
    include_vuln_types TEXT COMMENT '包含的漏洞类型',
    scan_policy TEXT COMMENT '扫描策略',
    
    -- 审计字段
    created_by BIGINT COMMENT '创建者ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记',
    
    -- 索引
    INDEX idx_scan_configs_name (name),
    INDEX idx_scan_configs_engine (scan_engine),
    INDEX idx_scan_configs_type (scan_type),
    INDEX idx_scan_configs_enabled (enabled),
    INDEX idx_scan_configs_default (is_default),
    INDEX idx_scan_configs_creator (created_by),
    INDEX idx_scan_configs_deleted (deleted),
    INDEX idx_scan_configs_created_time (created_time),
    
    -- 外键约束
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扫描配置表';

-- 添加唯一约束
ALTER TABLE scan_configs ADD CONSTRAINT uk_scan_configs_name UNIQUE (name, deleted);

-- 为ScanTask表添加scan_config_id字段（如果不存在）
ALTER TABLE scan_tasks ADD COLUMN scan_config_id BIGINT COMMENT '扫描配置ID' AFTER scan_template;
ALTER TABLE scan_tasks ADD INDEX idx_scan_tasks_config (scan_config_id);
ALTER TABLE scan_tasks ADD CONSTRAINT fk_scan_tasks_config 
    FOREIGN KEY (scan_config_id) REFERENCES scan_configs(id) ON DELETE SET NULL; 