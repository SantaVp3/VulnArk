-- 添加通知系统相关表

USE vulnark;

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    title VARCHAR(255) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    level VARCHAR(20) DEFAULT 'info' COMMENT '通知级别',
    recipient_id BIGINT NOT NULL COMMENT '接收人ID',
    sender_id BIGINT NULL COMMENT '发送人ID',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    data JSON COMMENT '通知数据',
    entity_type VARCHAR(50) COMMENT '关联实体类型',
    entity_id BIGINT COMMENT '关联实体ID',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_type (type),
    INDEX idx_level (level),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_expires_at (expires_at),
    INDEX idx_entity (entity_type, entity_id),
    FOREIGN KEY (recipient_id) REFERENCES users(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
) COMMENT '通知表';

-- 邮件模板表
CREATE TABLE IF NOT EXISTS email_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '模板名称',
    subject VARCHAR(255) NOT NULL COMMENT '邮件主题',
    content LONGTEXT NOT NULL COMMENT '邮件内容',
    type VARCHAR(50) NOT NULL COMMENT '模板类型',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    variables TEXT COMMENT '模板变量说明',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_is_active (is_active),
    INDEX idx_creator_id (creator_id),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) COMMENT '邮件模板表';

-- 邮件发送日志表
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `to` VARCHAR(255) NOT NULL COMMENT '收件人',
    cc VARCHAR(500) COMMENT '抄送',
    bcc VARCHAR(500) COMMENT '密送',
    subject VARCHAR(255) NOT NULL COMMENT '邮件主题',
    content LONGTEXT COMMENT '邮件内容',
    template_id BIGINT NULL COMMENT '使用的模板ID',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '发送状态',
    error TEXT COMMENT '错误信息',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    sender_id BIGINT NULL COMMENT '发送人ID',
    INDEX idx_to (`to`),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_sent_at (sent_at),
    INDEX idx_template_id (template_id),
    INDEX idx_sender_id (sender_id),
    FOREIGN KEY (template_id) REFERENCES email_templates(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
) COMMENT '邮件发送日志表';

-- 通知设置表
CREATE TABLE IF NOT EXISTS notification_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    email_enabled BOOLEAN DEFAULT TRUE COMMENT '邮件通知开关',
    system_enabled BOOLEAN DEFAULT TRUE COMMENT '系统通知开关',
    vulnerability_assigned BOOLEAN DEFAULT TRUE COMMENT '漏洞分配通知',
    vulnerability_updated BOOLEAN DEFAULT TRUE COMMENT '漏洞更新通知',
    report_submitted BOOLEAN DEFAULT TRUE COMMENT '报告提交通知',
    report_reviewed BOOLEAN DEFAULT TRUE COMMENT '报告审核通知',
    system_maintenance BOOLEAN DEFAULT TRUE COMMENT '系统维护通知',
    security_alert BOOLEAN DEFAULT TRUE COMMENT '安全警报通知',
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '通知设置表';

-- 插入默认邮件模板
INSERT INTO email_templates (name, subject, content, type, creator_id, variables) VALUES
('vulnerability_assigned', '漏洞分配通知 - {{.VulnerabilityTitle}}', 
'<html><body><h2>漏洞分配通知</h2><p>您好 {{.AssigneeName}}，</p><p>有一个新的漏洞已分配给您处理：</p><ul><li><strong>漏洞标题：</strong>{{.VulnerabilityTitle}}</li><li><strong>严重程度：</strong>{{.Severity}}</li><li><strong>发现时间：</strong>{{.DiscoveredAt}}</li><li><strong>分配人：</strong>{{.AssignerName}}</li></ul><p>请及时登录系统查看详情并处理。</p><p>系统地址：<a href="{{.SystemURL}}">{{.SystemURL}}</a></p><br><p>此邮件由系统自动发送，请勿回复。</p></body></html>', 
'vulnerability', 1, 'AssigneeName, VulnerabilityTitle, Severity, DiscoveredAt, AssignerName, SystemURL'),

('report_submitted', '报告提交通知 - {{.ReportTitle}}', 
'<html><body><h2>报告提交通知</h2><p>您好，</p><p>有一个新的报告已提交，等待您的审核：</p><ul><li><strong>报告标题：</strong>{{.ReportTitle}}</li><li><strong>报告类型：</strong>{{.ReportType}}</li><li><strong>严重程度：</strong>{{.Severity}}</li><li><strong>提交人：</strong>{{.SubmitterName}}</li><li><strong>提交时间：</strong>{{.SubmittedAt}}</li></ul><p>请及时登录系统进行审核。</p><p>系统地址：<a href="{{.SystemURL}}">{{.SystemURL}}</a></p><br><p>此邮件由系统自动发送，请勿回复。</p></body></html>', 
'report', 1, 'ReportTitle, ReportType, Severity, SubmitterName, SubmittedAt, SystemURL'),

('report_reviewed', '报告审核结果通知 - {{.ReportTitle}}', 
'<html><body><h2>报告审核结果通知</h2><p>您好 {{.SubmitterName}}，</p><p>您提交的报告已完成审核：</p><ul><li><strong>报告标题：</strong>{{.ReportTitle}}</li><li><strong>审核结果：</strong>{{.ReviewResult}}</li><li><strong>审核人：</strong>{{.ReviewerName}}</li><li><strong>审核时间：</strong>{{.ReviewedAt}}</li>{{if .ReviewNotes}}<li><strong>审核备注：</strong>{{.ReviewNotes}}</li>{{end}}</ul><p>请登录系统查看详细信息。</p><p>系统地址：<a href="{{.SystemURL}}">{{.SystemURL}}</a></p><br><p>此邮件由系统自动发送，请勿回复。</p></body></html>', 
'report', 1, 'SubmitterName, ReportTitle, ReviewResult, ReviewerName, ReviewedAt, ReviewNotes, SystemURL'),

('system_maintenance', '系统维护通知', 
'<html><body><h2>系统维护通知</h2><p>您好，</p><p>系统将进行维护，具体信息如下：</p><ul><li><strong>维护时间：</strong>{{.MaintenanceTime}}</li><li><strong>预计时长：</strong>{{.Duration}}</li><li><strong>维护内容：</strong>{{.Description}}</li></ul><p>维护期间系统将暂停服务，请合理安排工作时间。</p><p>如有疑问，请联系系统管理员。</p><br><p>此邮件由系统自动发送，请勿回复。</p></body></html>', 
'system', 1, 'MaintenanceTime, Duration, Description');

-- 为所有现有用户创建默认通知设置
INSERT INTO notification_settings (user_id)
SELECT id FROM users 
WHERE NOT EXISTS (
    SELECT 1 FROM notification_settings ns WHERE ns.user_id = users.id
);
