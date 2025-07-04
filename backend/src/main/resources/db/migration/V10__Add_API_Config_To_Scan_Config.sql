-- 为扫描配置表添加API配置相关字段
ALTER TABLE scan_configs 
ADD COLUMN api_endpoint VARCHAR(500) NULL COMMENT 'API端点URL',
ADD COLUMN api_key VARCHAR(200) NULL COMMENT 'API访问密钥',
ADD COLUMN api_secret VARCHAR(200) NULL COMMENT 'API密钥',
ADD COLUMN api_username VARCHAR(100) NULL COMMENT 'API用户名',
ADD COLUMN api_password VARCHAR(200) NULL COMMENT 'API密码',
ADD COLUMN api_token VARCHAR(500) NULL COMMENT 'API认证令牌',
ADD COLUMN api_version VARCHAR(20) NULL COMMENT 'API版本',
ADD COLUMN ssl_verification BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用SSL验证',
ADD COLUMN connection_timeout INT NULL DEFAULT 30 COMMENT '连接超时时间（秒）',
ADD COLUMN read_timeout INT NULL DEFAULT 300 COMMENT '读取超时时间（秒）',
ADD COLUMN extended_config TEXT NULL COMMENT '扩展配置（JSON格式）';

-- 创建索引以提高查询性能
CREATE INDEX idx_scan_configs_api_endpoint ON scan_configs(api_endpoint);
CREATE INDEX idx_scan_configs_scan_engine ON scan_configs(scan_engine);

-- 更新现有配置的默认API信息
UPDATE scan_configs 
SET 
    api_version = '1.0',
    connection_timeout = 30,
    read_timeout = 300,
    ssl_verification = TRUE
WHERE api_version IS NULL; 