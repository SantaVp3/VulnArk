-- 添加知识库系统相关表（清理版）

USE vulnark;

-- 知识库表
CREATE TABLE IF NOT EXISTS knowledge_bases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content LONGTEXT NOT NULL COMMENT '内容',
    summary TEXT COMMENT '摘要',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    type VARCHAR(50) NOT NULL COMMENT '类型',
    severity VARCHAR(20) COMMENT '严重程度',
    status VARCHAR(20) DEFAULT 'published' COMMENT '状态',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞次数',
    author_id INT NOT NULL COMMENT '作者ID',
    reviewer_id INT NULL COMMENT '审核人ID',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    tags VARCHAR(500) COMMENT '标签',
    metadata JSON COMMENT '元数据',
    INDEX idx_category (category),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_author_id (author_id)
) COMMENT '知识库表';

-- 知识库标签表
CREATE TABLE IF NOT EXISTS knowledge_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL COMMENT '知识库ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_knowledge_tag (knowledge_id, tag_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_tag_id (tag_id)
) COMMENT '知识库标签关联表';

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    description VARCHAR(255) COMMENT '标签描述',
    color VARCHAR(7) DEFAULT '#007bff' COMMENT '标签颜色',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) COMMENT '标签表';

-- 知识库评论表
CREATE TABLE IF NOT EXISTS knowledge_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL COMMENT '知识库ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parent_id BIGINT NULL COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) COMMENT '知识库评论表';

-- 知识库收藏表
CREATE TABLE IF NOT EXISTS knowledge_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL COMMENT '知识库ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_knowledge_user (knowledge_id, user_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_user_id (user_id)
) COMMENT '知识库收藏表';

-- 知识库访问记录表
CREATE TABLE IF NOT EXISTS knowledge_views (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL COMMENT '知识库ID',
    user_id BIGINT NULL COMMENT '用户ID（可为空，支持匿名访问）',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) COMMENT '知识库访问记录表';

-- 插入基础标签数据
INSERT INTO tags (name, description, color) VALUES
('sql-injection', 'SQL注入相关', '#dc3545'),
('xss', '跨站脚本攻击', '#fd7e14'),
('csrf', '跨站请求伪造', '#ffc107'),
('web', 'Web安全', '#007bff'),
('owasp', 'OWASP相关', '#28a745'),
('javascript', 'JavaScript相关', '#f1c40f'),
('burpsuite', 'Burp Suite工具', '#6f42c1'),
('penetration-testing', '渗透测试', '#e83e8c'),
('security', '安全相关', '#17a2b8'),
('vulnerability', '漏洞相关', '#dc3545');
