-- 添加报告管理相关表

USE vulnark;

-- 报告表
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    title VARCHAR(255) NOT NULL COMMENT '报告标题',
    description TEXT COMMENT '报告描述',
    type VARCHAR(50) NOT NULL COMMENT '报告类型',
    severity VARCHAR(20) DEFAULT 'medium' COMMENT '严重程度',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    asset_ids JSON COMMENT '关联资产ID列表',
    tags JSON COMMENT '标签列表',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) COMMENT '文件类型',
    download_url VARCHAR(500) COMMENT '下载URL',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    test_date DATE COMMENT '测试日期',
    submitted_at TIMESTAMP NULL COMMENT '提交时间',
    reviewed_at TIMESTAMP NULL COMMENT '审核时间',
    reviewer_id BIGINT NULL COMMENT '审核人ID',
    review_notes TEXT COMMENT '审核备注',
    INDEX idx_type (type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_uploader_id (uploader_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_created_at (created_at),
    INDEX idx_test_date (test_date),
    FOREIGN KEY (uploader_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
) COMMENT '报告表';

-- 报告评论表
CREATE TABLE IF NOT EXISTS report_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    report_id BIGINT NOT NULL COMMENT '报告ID',
    content TEXT NOT NULL COMMENT '评论内容',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    INDEX idx_report_id (report_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT '报告评论表';

-- 文件附件表
CREATE TABLE IF NOT EXISTS file_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) COMMENT '文件类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    download_url VARCHAR(500) COMMENT '下载URL',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    entity_type VARCHAR(50) COMMENT '实体类型',
    entity_id BIGINT COMMENT '实体ID',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    INDEX idx_uploader_id (uploader_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created_at (created_at),
    INDEX idx_expires_at (expires_at),
    FOREIGN KEY (uploader_id) REFERENCES users(id)
) COMMENT '文件附件表';

-- 创建uploads目录（如果不存在）
-- 这个需要在应用启动时通过代码创建
