-- 添加漏洞分配和生命周期管理相关表

USE vulnark;

-- 漏洞时间线表
CREATE TABLE IF NOT EXISTS vulnerability_timeline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    vulnerability_id BIGINT NOT NULL COMMENT '漏洞ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    description TEXT COMMENT '操作描述',
    old_value TEXT COMMENT '旧值',
    new_value TEXT COMMENT '新值',
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    INDEX idx_vulnerability_id (vulnerability_id),
    INDEX idx_action (action),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (vulnerability_id) REFERENCES vulnerabilities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT '漏洞时间线表';

-- 为现有漏洞创建初始时间线记录
INSERT INTO vulnerability_timeline (vulnerability_id, action, description, user_id)
SELECT id, 'created', CONCAT('漏洞"', title, '"被创建'), COALESCE(discoverer_id, 1)
FROM vulnerabilities
WHERE NOT EXISTS (
    SELECT 1 FROM vulnerability_timeline vt 
    WHERE vt.vulnerability_id = vulnerabilities.id AND vt.action = 'created'
);
