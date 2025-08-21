-- 添加AI助手相关表
USE vulnark;

-- AI配置表
CREATE TABLE IF NOT EXISTS ai_configurations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL COMMENT '用户ID',
    provider VARCHAR(50) NOT NULL DEFAULT 'openai' COMMENT 'AI提供商：openai, deepseek, qwen, custom',
    api_key VARCHAR(500) COMMENT 'API密钥',
    api_url VARCHAR(255) COMMENT '自定义API端点',
    model VARCHAR(100) COMMENT '使用的模型名称',
    max_tokens INT DEFAULT 2000 COMMENT '最大token数',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数',
    timeout INT DEFAULT 30 COMMENT '请求超时时间（秒）',
    enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用',
    
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_provider (provider),
    INDEX idx_enabled (enabled),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'AI配置表';

-- AI对话表
CREATE TABLE IF NOT EXISTS ai_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    title VARCHAR(255) COMMENT '对话标题',
    messages LONGTEXT COMMENT '对话消息（JSON格式）',
    tokens_used INT DEFAULT 0 COMMENT '使用的token数',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active, archived, deleted',
    
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'AI对话表';

-- AI使用统计表（可选，用于详细统计）
CREATE TABLE IF NOT EXISTS ai_usage_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    provider VARCHAR(50) NOT NULL COMMENT 'AI提供商',
    model VARCHAR(100) COMMENT '使用的模型',
    prompt_tokens INT DEFAULT 0 COMMENT '输入token数',
    completion_tokens INT DEFAULT 0 COMMENT '输出token数',
    total_tokens INT DEFAULT 0 COMMENT '总token数',
    response_time INT COMMENT '响应时间（毫秒）',
    status VARCHAR(20) DEFAULT 'success' COMMENT '状态：success, error, timeout',
    error_message TEXT COMMENT '错误信息',
    
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_provider (provider),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'AI使用日志表';

-- 插入默认AI配置（为现有用户创建默认配置）
INSERT IGNORE INTO ai_configurations (user_id, provider, max_tokens, temperature, timeout, enabled)
SELECT id, 'openai', 2000, 0.70, 30, FALSE
FROM users;

-- 创建AI助手系统配置
INSERT IGNORE INTO system_configs (`key`, value, type, category, display_name, description, is_public, is_editable, default_value) VALUES
('ai.enabled', '{"value": false}', 'boolean', 'ai', 'AI助手功能', '是否启用AI助手功能', TRUE, TRUE, '{"value": false}'),
('ai.default_provider', '{"value": "openai"}', 'string', 'ai', '默认AI提供商', '系统默认的AI服务提供商', FALSE, TRUE, '{"value": "openai"}'),
('ai.max_conversations_per_user', '{"value": 50}', 'int', 'ai', '用户最大对话数', '每个用户最多可保存的对话数量', FALSE, TRUE, '{"value": 50}'),
('ai.max_tokens_per_request', '{"value": 4000}', 'int', 'ai', '单次请求最大Token', '单次AI请求的最大token限制', FALSE, TRUE, '{"value": 4000}'),
('ai.rate_limit_per_hour', '{"value": 100}', 'int', 'ai', '每小时请求限制', '每个用户每小时最多AI请求次数', FALSE, TRUE, '{"value": 100}');
