-- VulnArk 初始数据脚本
USE vulnark;

-- 插入角色数据
INSERT INTO roles (id, name, description, permissions) VALUES
(1, '管理员', '系统管理员，拥有所有权限', JSON_ARRAY(
    'user:create', 'user:read', 'user:update', 'user:delete',
    'asset:create', 'asset:read', 'asset:update', 'asset:delete',
    'vulnerability:create', 'vulnerability:read', 'vulnerability:update', 'vulnerability:delete',
    'vulnerability:assign', 'vulnerability:verify',
    'report:create', 'report:read', 'report:update', 'report:delete',
    'knowledge:create', 'knowledge:read', 'knowledge:update', 'knowledge:delete',
    'notification:send', 'statistics:view', 'settings:manage'
)),
(2, '开发团队', '开发人员，负责漏洞修复', JSON_ARRAY(
    'asset:read', 'vulnerability:read', 'vulnerability:update',
    'report:create', 'report:read', 'report:update',
    'knowledge:read', 'notification:read'
)),
(3, '普通用户', '普通用户，仅查看权限', JSON_ARRAY(
    'vulnerability:read', 'report:read', 'knowledge:read', 'notification:read'
));

-- 管理员用户将通过系统初始化代码自动创建，使用随机密码

-- 插入OWASP Top10 2021分类数据
INSERT INTO owasp_categories (id, code, name, description, year, severity_level) VALUES
(1, 'A01:2021', '失效的访问控制', '访问控制强制执行策略失效，攻击者可以绕过授权并执行用户功能或访问未授权数据', 2021, 1),
(2, 'A02:2021', '加密机制失效', '与加密相关的失效（或缺乏加密），通常导致敏感数据暴露', 2021, 1),
(3, 'A03:2021', '注入', '当不受信任的数据作为命令或查询的一部分发送到解释器时发生', 2021, 1),
(4, 'A04:2021', '不安全设计', '代表了与设计和架构缺陷相关的不同弱点类别', 2021, 2),
(5, 'A05:2021', '安全配置错误', '应用程序可能配置错误如果应用程序堆栈的任何部分配置不当', 2021, 2),
(6, 'A06:2021', '易受攻击和过时的组件', '使用已知漏洞的组件', 2021, 2),
(7, 'A07:2021', '身份识别和身份验证失效', '与用户身份确认、身份验证和会话管理相关的应用程序功能', 2021, 1),
(8, 'A08:2021', '软件和数据完整性失效', '与软件更新、关键数据和CI/CD管道相关的代码和基础设施', 2021, 2),
(9, 'A09:2021', '安全日志和监控失效', '与可见性、事件检测、警报和取证相关', 2021, 3),
(10, 'A10:2021', '服务器端请求伪造', 'SSRF缺陷发生在Web应用程序获取远程资源而不验证用户提供的URL时', 2021, 2);

-- 插入示例资产数据
INSERT INTO assets (id, name, type, category, ip_address, domain, department, importance_level, description, status) VALUES
(1, 'Web服务器-01', 'server', 'Web服务器', '192.168.1.100', 'web01.example.com', 'IT部门', 1, '主要业务Web服务器', 1),
(2, 'MySQL数据库', 'database', '关系型数据库', '192.168.1.101', 'db01.example.com', 'IT部门', 1, '核心业务数据库', 1),
(3, '用户管理系统', 'application', 'Web应用', '192.168.1.102', 'user.example.com', '开发部门', 2, '用户管理Web应用', 1),
(4, '防火墙设备', 'network', '安全设备', '192.168.1.1', '', 'IT部门', 1, '网络边界防火墙', 1);

-- 插入示例知识库数据
INSERT INTO knowledge_base (id, title, content, category, tags, vulnerability_type, fix_template, author_id, is_public) VALUES
(1, 'SQL注入漏洞修复指南',
'SQL注入是一种代码注入技术，攻击者通过在应用程序的输入字段中插入恶意SQL代码来攻击数据库。

修复方法：
1. 使用参数化查询（预编译语句）
2. 输入验证和过滤
3. 最小权限原则
4. 错误信息处理

实施步骤：
- 检查所有数据库查询是否使用参数化查询
- 验证所有用户输入
- 实施最小权限原则
- 配置错误页面不显示敏感信息',
'安全修复', JSON_ARRAY('SQL注入', '数据库安全', '代码审计'), 'A03:2021',
'1. 检查所有数据库查询是否使用参数化查询\\n2. 验证所有用户输入\\n3. 实施最小权限原则\\n4. 配置错误页面不显示敏感信息',
1, 1),

(2, 'XSS跨站脚本攻击防护',
'跨站脚本攻击(XSS)是一种注入攻击，攻击者将恶意脚本注入到其他用户会浏览的网页中。

防护措施：
1. 输出编码
2. 输入验证
3. 内容安全策略(CSP)
4. HttpOnly Cookie

实施步骤：
- 对所有用户输入进行HTML编码
- 实施严格的输入验证
- 配置CSP头部
- 设置安全的Cookie属性',
'安全修复', JSON_ARRAY('XSS', 'Web安全', '前端安全'), 'A07:2021',
'1. 实施输出编码\\n2. 配置内容安全策略\\n3. 验证和过滤用户输入\\n4. 设置安全Cookie属性',
1, 1);

-- 通知将在管理员用户创建后自动添加

-- 重置自增ID
ALTER TABLE roles AUTO_INCREMENT = 4;
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE owasp_categories AUTO_INCREMENT = 11;
ALTER TABLE assets AUTO_INCREMENT = 5;
ALTER TABLE knowledge_base AUTO_INCREMENT = 3;
ALTER TABLE notifications AUTO_INCREMENT = 1;
