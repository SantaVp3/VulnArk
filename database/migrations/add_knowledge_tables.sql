-- 添加知识库系统相关表

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
    reviewed_at TIMESTAMP NULL COMMENT '审核时间',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    tags TEXT COMMENT '标签（逗号分隔）',
    metadata JSON COMMENT '元数据',
    INDEX idx_title (title),
    INDEX idx_category (category),
    INDEX idx_type (type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_author_id (author_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_view_count (view_count),
    INDEX idx_like_count (like_count),
    INDEX idx_created_at (created_at),
    INDEX idx_published_at (published_at),
    FULLTEXT idx_content (title, content, summary),
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
) COMMENT '知识库表';

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    description TEXT COMMENT '标签描述',
    color VARCHAR(7) DEFAULT '#007bff' COMMENT '标签颜色',
    category VARCHAR(50) COMMENT '标签分类',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    creator_id INT NOT NULL COMMENT '创建者ID',
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_usage_count (usage_count),
    INDEX idx_creator_id (creator_id),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) COMMENT '标签表';

-- 知识库标签关联表
CREATE TABLE IF NOT EXISTS knowledge_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL COMMENT '知识库ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY uk_knowledge_tag (knowledge_id, tag_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_tag_id (tag_id),
    FOREIGN KEY (knowledge_id) REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) COMMENT '知识库标签关联表';

-- 模板表
CREATE TABLE IF NOT EXISTS templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '模板名称',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    description TEXT COMMENT '模板描述',
    type VARCHAR(50) NOT NULL COMMENT '模板类型',
    category VARCHAR(50) NOT NULL COMMENT '模板分类',
    content LONGTEXT NOT NULL COMMENT '模板内容',
    variables TEXT COMMENT '模板变量说明',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    creator_id INT NOT NULL COMMENT '创建者ID',
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_category (category),
    INDEX idx_is_active (is_active),
    INDEX idx_usage_count (usage_count),
    INDEX idx_creator_id (creator_id),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) COMMENT '模板表';

-- 插入默认标签
INSERT INTO tags (name, display_name, description, color, category, creator_id) VALUES
('sql-injection', 'SQL注入', 'SQL注入漏洞相关', '#dc3545', 'vulnerability', 1),
('xss', 'XSS跨站脚本', '跨站脚本攻击相关', '#fd7e14', 'vulnerability', 1),
('csrf', 'CSRF跨站请求伪造', '跨站请求伪造相关', '#ffc107', 'vulnerability', 1),
('rce', '远程代码执行', '远程代码执行漏洞', '#dc3545', 'vulnerability', 1),
('lfi', '本地文件包含', '本地文件包含漏洞', '#6f42c1', 'vulnerability', 1),
('rfi', '远程文件包含', '远程文件包含漏洞', '#6f42c1', 'vulnerability', 1),
('xxe', 'XXE外部实体注入', 'XML外部实体注入', '#e83e8c', 'vulnerability', 1),
('ssrf', 'SSRF服务端请求伪造', '服务端请求伪造', '#20c997', 'vulnerability', 1),
('deserialization', '反序列化', '反序列化漏洞', '#17a2b8', 'vulnerability', 1),
('privilege-escalation', '权限提升', '权限提升漏洞', '#dc3545', 'vulnerability', 1),
('web', 'Web安全', 'Web应用安全', '#007bff', 'security', 1),
('network', '网络安全', '网络层安全', '#28a745', 'security', 1),
('system', '系统安全', '操作系统安全', '#6c757d', 'security', 1),
('mobile', '移动安全', '移动应用安全', '#fd7e14', 'security', 1),
('cloud', '云安全', '云计算安全', '#20c997', 'security', 1),
('burpsuite', 'Burp Suite', 'Burp Suite工具使用', '#ff6b6b', 'tool', 1),
('nmap', 'Nmap', 'Nmap网络扫描工具', '#4ecdc4', 'tool', 1),
('metasploit', 'Metasploit', 'Metasploit渗透测试框架', '#45b7d1', 'tool', 1),
('wireshark', 'Wireshark', 'Wireshark网络协议分析', '#96ceb4', 'tool', 1),
('owasp', 'OWASP', 'OWASP安全项目', '#ffeaa7', 'framework', 1),
('java', 'Java', 'Java语言相关', '#e17055', 'language', 1),
('python', 'Python', 'Python语言相关', '#74b9ff', 'language', 1),
('php', 'PHP', 'PHP语言相关', '#a29bfe', 'language', 1),
('javascript', 'JavaScript', 'JavaScript语言相关', '#fdcb6e', 'language', 1),
('c-sharp', 'C#', 'C#语言相关', '#6c5ce7', 'language', 1);

-- 插入默认模板
INSERT INTO templates (name, display_name, description, type, category, content, variables, creator_id) VALUES
('vulnerability_report', '漏洞报告模板', '标准漏洞报告模板', 'vulnerability_report', 'report', 
'# 漏洞报告

## 基本信息
- **漏洞名称**: {{.VulnerabilityName}}
- **发现时间**: {{.DiscoveredAt}}
- **严重程度**: {{.Severity}}
- **影响系统**: {{.AffectedSystem}}

## 漏洞描述
{{.Description}}

## 复现步骤
{{.ReproductionSteps}}

## 影响分析
{{.ImpactAnalysis}}

## 修复建议
{{.FixSuggestion}}

## 参考资料
{{.References}}', 
'VulnerabilityName, DiscoveredAt, Severity, AffectedSystem, Description, ReproductionSteps, ImpactAnalysis, FixSuggestion, References', 1),

('security_guide', '安全指南模板', '安全操作指南模板', 'security_guide', 'guide',
'# {{.Title}}

## 概述
{{.Overview}}

## 适用范围
{{.Scope}}

## 安全要求
{{.SecurityRequirements}}

## 操作步骤
{{.Steps}}

## 注意事项
{{.Precautions}}

## 相关文档
{{.RelatedDocuments}}',
'Title, Overview, Scope, SecurityRequirements, Steps, Precautions, RelatedDocuments', 1),

('fix_solution', '修复方案模板', '漏洞修复方案模板', 'fix_solution', 'solution',
'# 修复方案：{{.VulnerabilityName}}

## 问题描述
{{.ProblemDescription}}

## 根本原因
{{.RootCause}}

## 修复方案
{{.Solution}}

## 实施步骤
{{.ImplementationSteps}}

## 验证方法
{{.VerificationMethod}}

## 风险评估
{{.RiskAssessment}}

## 回滚计划
{{.RollbackPlan}}',
'VulnerabilityName, ProblemDescription, RootCause, Solution, ImplementationSteps, VerificationMethod, RiskAssessment, RollbackPlan', 1),

('test_case', '测试用例模板', '安全测试用例模板', 'test_case', 'test',
'# 测试用例：{{.TestCaseName}}

## 测试目标
{{.TestObjective}}

## 前置条件
{{.Prerequisites}}

## 测试步骤
{{.TestSteps}}

## 预期结果
{{.ExpectedResult}}

## 实际结果
{{.ActualResult}}

## 测试数据
{{.TestData}}

## 备注
{{.Notes}}',
'TestCaseName, TestObjective, Prerequisites, TestSteps, ExpectedResult, ActualResult, TestData, Notes', 1);

-- 插入示例知识库条目
INSERT INTO knowledge_bases (title, content, summary, category, type, severity, status, author_id, published_at, tags, metadata) VALUES
('SQL注入漏洞防护指南', 
'# SQL注入漏洞防护指南

## 什么是SQL注入

SQL注入是一种常见的Web应用安全漏洞，攻击者通过在应用程序的输入字段中插入恶意SQL代码，来操作后端数据库。

## 常见的SQL注入类型

1. **基于错误的SQL注入**
2. **基于布尔的盲注**
3. **基于时间的盲注**
4. **联合查询注入**

## 防护措施

### 1. 使用参数化查询
```sql
-- 错误示例
SELECT * FROM users WHERE id = '' + userId + '';

-- 正确示例
SELECT * FROM users WHERE id = ?;
```

### 2. 输入验证和过滤
- 对所有用户输入进行严格验证
- 使用白名单过滤特殊字符
- 限制输入长度

### 3. 最小权限原则
- 数据库用户只授予必要的权限
- 禁用不必要的数据库功能
- 定期审查权限设置

## 检测工具

- SQLMap
- Burp Suite
- OWASP ZAP

## 参考资料

- [OWASP SQL Injection Prevention Cheat Sheet](https://owasp.org/www-community/attacks/SQL_Injection)
- [CWE-89: Improper Neutralization of Special Elements used in an SQL Command](https://cwe.mitre.org/data/definitions/89.html)',
'SQL注入是最常见的Web安全漏洞之一，本文详细介绍了SQL注入的类型、危害以及有效的防护措施。',
'vulnerability', 'guide', 'high', 'published', 1, NOW(), 'sql-injection,web,security', 
'{"author": "安全团队", "version": "1.0", "last_updated": "2024-01-07"}'),

('XSS跨站脚本攻击防护', 
'# XSS跨站脚本攻击防护

## XSS攻击类型

### 1. 反射型XSS
恶意脚本通过URL参数或表单提交，在服务器响应中直接反射给用户。

### 2. 存储型XSS
恶意脚本被存储在服务器端，当其他用户访问包含恶意脚本的页面时被执行。

### 3. DOM型XSS
恶意脚本通过修改页面DOM结构来执行，不需要服务器参与。

## 防护策略

### 1. 输出编码
```javascript
// HTML编码
function htmlEncode(str) {
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/\'/g, "&#x27;");
}
```

### 2. 内容安全策略(CSP)
```html
<meta http-equiv="Content-Security-Policy" 
      content="default-src ''self''; script-src ''self'' ''unsafe-inline''">
```

### 3. 输入验证
- 白名单验证
- 长度限制
- 格式检查

## 检测方法

- 手工测试
- 自动化扫描工具
- 代码审计

## 修复建议

1. 对所有用户输入进行适当的编码
2. 实施严格的CSP策略
3. 使用安全的JavaScript API
4. 定期进行安全测试',
'XSS是Web应用中的高危漏洞，本文介绍了XSS的类型、检测方法和防护策略。',
'vulnerability', 'guide', 'high', 'published', 1, NOW(), 'xss,web,security',
'{"author": "安全团队", "version": "1.0", "difficulty": "中级"}'),

('Burp Suite使用教程', 
'# Burp Suite使用教程

## 简介

Burp Suite是一个集成的Web应用安全测试平台，包含多个工具用于执行Web应用安全测试。

## 主要功能模块

### 1. Proxy代理
- 拦截HTTP/HTTPS请求和响应
- 修改请求参数
- 查看完整的请求响应数据

### 2. Spider爬虫
- 自动发现Web应用的内容和功能
- 构建站点地图
- 识别攻击面

### 3. Scanner扫描器
- 自动检测常见漏洞
- 生成详细的扫描报告
- 支持自定义扫描策略

### 4. Intruder暴力破解
- 自动化攻击工具
- 支持多种攻击类型
- 可自定义payload

### 5. Repeater重放器
- 手动修改和重放请求
- 分析响应差异
- 验证漏洞

## 基本使用流程

1. **配置代理**
   - 设置浏览器代理为127.0.0.1:8080
   - 安装Burp CA证书

2. **目标设置**
   - 添加目标域名到Scope
   - 配置目标范围

3. **被动扫描**
   - 浏览目标应用
   - 观察Proxy历史记录

4. **主动测试**
   - 使用Scanner进行自动扫描
   - 使用Intruder进行定制攻击

## 高级技巧

- 使用Burp扩展增强功能
- 自定义扫描规则
- 集成其他安全工具

## 注意事项

- 仅在授权的系统上使用
- 注意扫描频率，避免影响业务
- 及时更新工具版本',
'Burp Suite是Web安全测试的必备工具，本教程详细介绍了其主要功能和使用方法。',
'tool', 'tutorial', 'medium', 'published', 1, NOW(), 'burpsuite,tool,web,penetration-testing',
'{"author": "渗透测试团队", "version": "1.0", "tool_version": "2023.12"}');

-- 创建知识库标签关联
INSERT INTO knowledge_tags (knowledge_id, tag_id) 
SELECT kb.id, t.id 
FROM knowledge_bases kb, tags t 
WHERE kb.title = 'SQL注入漏洞防护指南' AND t.name IN ('sql-injection', 'web', 'owasp');

INSERT INTO knowledge_tags (knowledge_id, tag_id) 
SELECT kb.id, t.id 
FROM knowledge_bases kb, tags t 
WHERE kb.title = 'XSS跨站脚本攻击防护' AND t.name IN ('xss', 'web', 'javascript');

INSERT INTO knowledge_tags (knowledge_id, tag_id) 
SELECT kb.id, t.id 
FROM knowledge_bases kb, tags t 
WHERE kb.title = 'Burp Suite使用教程' AND t.name IN ('burpsuite', 'web');
