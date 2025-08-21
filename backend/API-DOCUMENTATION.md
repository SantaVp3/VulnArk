# VulnArk API 完整文档

## 📋 目录

- [API概述](#api概述)
- [安全机制](#安全机制)
- [响应格式](#响应格式)
- [错误处理](#错误处理)
- [速率限制](#速率限制)
- [API接口清单](#api接口清单)
- [使用示例](#使用示例)
- [安全使用指南](#安全使用指南)
- [Postman集合](#postman集合)

---

## 🔍 API概述

**基础信息**
- **API版本**: v1.0.0
- **基础URL**: `http://localhost:8080/api/v1`
- **协议**: HTTP/HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8

**API特性**
- RESTful API设计
- JWT认证机制
- 基于角色的权限控制(RBAC)
- 统一响应格式
- 请求参数验证
- 分页查询支持
- API访问频率限制
- 安全错误处理

**安全更新**
- ✅ 实现了完整的权限中间件系统
- ✅ 修复了API测试工具的SSRF漏洞
- ✅ 添加了API访问频率限制
- ✅ 加强了分页参数限制（最大50条/页）
- ✅ 优化了错误信息处理，避免敏感信息泄露
- ✅ 添加了输入长度限制

---

## 🔐 安全机制

### JWT认证

所有需要认证的接口都需要在请求头中包含JWT令牌：

```http
Authorization: Bearer <your-jwt-token>
```

### 权限控制

API接口按照权限要求分为以下级别：

1. **公开接口** - 无需认证和权限
2. **认证接口** - 需要有效JWT令牌
3. **权限接口** - 需要特定权限代码

**权限代码格式**: `module:action`

**权限模块**:
- `user` - 用户管理
- `role` - 角色管理
- `asset` - 资产管理
- `vulnerability` - 漏洞管理
- `report` - 报告管理
- `system` - 系统管理
- `analytics` - 统计分析
- `api` - API测试

**权限操作**:
- `read` - 查看权限
- `write` - 编辑权限
- `manage` - 管理权限（包含增删改查）
- `delete` - 删除权限
- `export` - 导出权限
- `assign` - 分配权限

### 获取令牌

通过登录接口获取JWT令牌：

```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_at": "2025-08-13T10:30:00Z",
    "user": {
      "id": 1,
      "username": "admin",
      "real_name": "管理员",
      "role": {
        "id": 1,
        "name": "超级管理员"
      }
    }
  }
}
```

---

## 📊 响应格式

### 统一响应结构

所有API接口都使用统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**字段说明**:
- `code`: HTTP状态码
- `message`: 响应消息
- `data`: 响应数据（可选）

### 分页响应格式

对于列表查询接口，使用分页响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "page": 1,
    "page_size": 10,
    "total_pages": 10,
    "data": []
  }
}
```

**分页参数限制**:
- `page`: 1-1000
- `page_size`: 1-50（最大50条/页）

---

## ❌ 错误处理

### HTTP状态码

- `200` - 请求成功
- `400` - 请求参数错误
- `401` - 未授权访问
- `403` - 权限不足
- `404` - 资源不存在
- `429` - 请求过于频繁
- `500` - 服务器内部错误

### 错误响应示例

```json
{
  "code": 400,
  "message": "请求参数格式错误"
}
```

**安全特性**:
- 错误信息已净化，不会泄露敏感系统信息
- 避免暴露内部路径、数据库信息等
- 统一的错误消息格式

---

## ⏱️ 速率限制

### 限制策略

| 接口类型 | 限制规则 | 说明 |
|---------|---------|------|
| 全局限制 | 100请求/秒 | 基于IP地址 |
| API接口 | 50请求/秒 | 基于IP+路径 |
| 登录接口 | 5请求/分钟 | 基于IP地址 |
| 上传接口 | 10请求/分钟 | 基于用户ID或IP |
| API测试 | 50请求/秒 | 基于用户ID |

### 速率限制响应

当触发速率限制时，返回429状态码：

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试"
}
```

**响应头**:
- `X-RateLimit-Limit`: 限制数量
- `X-RateLimit-Remaining`: 剩余请求数
- `Retry-After`: 重试等待时间（秒）

---

## 📋 API接口清单

### 📊 接口统计

| 模块 | 接口数量 | 认证要求 | 权限要求 | 安全状态 |
|------|---------|---------|---------|---------|
| 公开接口 | 10 | ❌ | ❌ | ✅ 安全 |
| 认证接口 | 1 | ❌ | ❌ | ✅ 已加速率限制 |
| 用户管理 | 8 | ✅ | user:manage | ✅ 已启用权限 |
| 角色管理 | 5 | ✅ | role:manage | ✅ 已启用权限 |
| 资产管理 | 9 | ✅ | asset:* | ✅ 已启用权限 |
| 漏洞管理 | 15 | ✅ | vulnerability:* | ✅ 已启用权限 |
| 报告管理 | 9 | ✅ | report:* | ✅ 已启用权限 |
| 通知管理 | 15 | ✅ | notification:* | ✅ 已启用权限 |
| 权限管理 | 9 | ✅ | system:permission | ✅ 已启用权限 |
| 系统管理 | 8 | ✅ | system:* | ✅ 已启用权限 |
| 知识库管理 | 19 | ✅ | knowledge:* | ✅ 已启用权限 |
| 统计分析 | 7 | ✅ | analytics:* | ✅ 已启用权限 |
| API测试工具 | 5 | ✅ | api:test | ✅ 已修复SSRF |

**总计**: 125个API接口，全部已实现安全防护

---

## 🌐 公开接口

### API版本管理

#### 获取API版本信息
- **接口**: `GET /api/versions`
- **描述**: 获取系统支持的所有API版本信息
- **认证**: 无需认证
- **速率限制**: 全局限制

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "current": {
      "version": "1.0.0",
      "status": "active",
      "release_date": "2025-08-07",
      "description": "VulnArk API 第一个正式版本"
    }
  }
}
```

#### 其他版本接口
- `GET /api/versions/:version` - 获取版本详情
- `GET /api/versions/compatibility` - 获取版本兼容性
- `GET /api/health` - API健康检查
- `GET /api/status` - API状态检查
- `GET /api/changelog` - API变更日志
- `GET /api/metrics` - API指标

### 公开系统接口

#### Ping测试
- **接口**: `GET /api/v1/public/ping`
- **响应**: `{"message": "pong"}`

#### 获取公开配置
- **接口**: `GET /api/v1/public/configs`
- **描述**: 获取系统公开配置信息

#### 获取系统信息
- **接口**: `GET /api/v1/public/system/info`
- **描述**: 获取系统基本信息

---

## 🔑 认证接口

### 用户登录
- **接口**: `POST /api/v1/login`
- **描述**: 用户登录获取JWT令牌
- **认证**: 无需认证
- **速率限制**: 5次/分钟（基于IP）

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**参数验证**:
- `username`: 必填，字符串
- `password`: 必填，字符串

**响应数据**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_at": "2025-08-13T10:30:00Z",
  "user": {
    "id": 1,
    "username": "admin",
    "real_name": "管理员",
    "email": "admin@vulnark.com",
    "role": {
      "id": 1,
      "name": "超级管理员"
    }
  }
}
```

**错误响应**:
- `400`: 参数错误
- `401`: 用户名或密码错误
- `429`: 登录尝试过于频繁

---

## 👤 用户管理

### 个人资料管理

#### 获取个人资料
- **接口**: `GET /api/v1/profile`
- **描述**: 获取当前登录用户的个人资料
- **认证**: 需要JWT令牌
- **权限**: 无需额外权限

**响应数据**:
```json
{
  "id": 1,
  "username": "admin",
  "real_name": "管理员",
  "email": "admin@vulnark.com",
  "phone": "13800138000",
  "department": "信息安全部",
  "avatar": "/uploads/avatars/admin.jpg",
  "last_login_at": "2025-08-12T10:30:00Z",
  "role": {
    "id": 1,
    "name": "超级管理员"
  }
}
```

#### 更新个人资料
- **接口**: `PUT /api/v1/profile`
- **描述**: 更新当前登录用户的个人资料
- **认证**: 需要JWT令牌
- **权限**: 无需额外权限

**请求参数**:
```json
{
  "real_name": "新姓名",
  "email": "new@vulnark.com",
  "phone": "13900139000",
  "department": "技术部",
  "avatar": "/uploads/avatars/new.jpg"
}
```

#### 修改密码
- **接口**: `POST /api/v1/change-password`
- **描述**: 修改当前登录用户的密码
- **认证**: 需要JWT令牌
- **权限**: 无需额外权限

**请求参数**:
```json
{
  "old_password": "old123",
  "new_password": "new123456"
}
```

**参数验证**:
- `old_password`: 必填，当前密码
- `new_password`: 必填，至少6位

### 用户管理（管理员功能）

#### 创建用户
- **接口**: `POST /api/v1/users`
- **描述**: 管理员创建新用户
- **认证**: 需要JWT令牌
- **权限**: `user:manage`

**请求参数**:
```json
{
  "username": "newuser",
  "email": "newuser@vulnark.com",
  "password": "password123",
  "real_name": "新用户",
  "phone": "13800138001",
  "role_id": 2,
  "department": "技术部"
}
```

**参数验证**:
- `username`: 必填，3-50字符，唯一
- `email`: 必填，邮箱格式，唯一
- `password`: 必填，至少6位
- `real_name`: 必填
- `role_id`: 必填，有效的角色ID

#### 获取用户列表
- **接口**: `GET /api/v1/users`
- **描述**: 获取用户列表（分页）
- **认证**: 需要JWT令牌
- **权限**: `user:manage`

**查询参数**:
- `page`: 页码，默认1，范围1-1000
- `page_size`: 每页数量，默认10，范围1-50
- `keyword`: 搜索关键词（用户名、姓名、邮箱），最大100字符
- `role_id`: 角色ID筛选
- `status`: 状态筛选（1-正常，0-禁用）

**请求示例**:
```bash
GET /api/v1/users?page=1&page_size=10&keyword=admin&role_id=1
```

#### 其他用户管理接口
- `GET /api/v1/users/:id` - 获取用户详情（权限：user:manage）
- `PUT /api/v1/users/:id` - 更新用户（权限：user:manage）
- `DELETE /api/v1/users/:id` - 删除用户（权限：user:manage）

---

## 🎭 角色管理

### 创建角色
- **接口**: `POST /api/v1/roles`
- **描述**: 创建新角色
- **认证**: 需要JWT令牌
- **权限**: `role:manage`

**请求参数**:
```json
{
  "name": "测试角色",
  "description": "测试角色描述",
  "status": 1
}
```

### 其他角色管理接口
- `GET /api/v1/roles` - 获取角色列表（权限：role:manage）
- `GET /api/v1/roles/:id` - 获取角色详情（权限：role:manage）
- `PUT /api/v1/roles/:id` - 更新角色（权限：role:manage）
- `DELETE /api/v1/roles/:id` - 删除角色（权限：role:manage）

---

## 🏢 资产管理

### 创建资产
- **接口**: `POST /api/v1/assets`
- **描述**: 创建新资产
- **认证**: 需要JWT令牌
- **权限**: `asset:manage`

**请求参数**:
```json
{
  "name": "Web服务器01",
  "type": "server",
  "category": "生产环境",
  "ip_address": "192.168.1.100",
  "domain": "web01.vulnark.com",
  "port": "80,443",
  "os": "Ubuntu 20.04",
  "version": "Apache 2.4",
  "department": "技术部",
  "owner_id": 1,
  "business_line": "核心业务",
  "importance_level": 1,
  "description": "主要Web服务器"
}
```

**参数验证**:
- `name`: 必填，1-100字符
- `type`: 必填，枚举值：server, database, application, network
- `ip_address`: 可选，IP地址格式
- `importance_level`: 可选，枚举值：1-高，2-中，3-低

### 获取资产列表
- **接口**: `GET /api/v1/assets`
- **描述**: 获取资产列表（分页）
- **认证**: 需要JWT令牌
- **权限**: `asset:read`

**查询参数**:
- `page`, `page_size`: 分页参数
- `keyword`: 搜索关键词，最大100字符
- `type`: 资产类型筛选
- `importance_level`: 重要性级别筛选
- `owner_id`: 负责人筛选

### 其他资产管理接口
- `GET /api/v1/assets/stats` - 获取资产统计（权限：asset:read）
- `GET /api/v1/assets/types` - 获取资产类型（权限：asset:read）
- `GET /api/v1/assets/importance-levels` - 获取重要性级别（权限：asset:read）
- `POST /api/v1/assets/import` - 批量导入资产（权限：asset:manage）
- `GET /api/v1/assets/:id` - 获取资产详情（权限：asset:read）
- `PUT /api/v1/assets/:id` - 更新资产（权限：asset:manage）
- `DELETE /api/v1/assets/:id` - 删除资产（权限：asset:manage）

---

## 🔍 漏洞管理

### 创建漏洞
- **接口**: `POST /api/v1/vulnerabilities`
- **描述**: 创建新漏洞
- **认证**: 需要JWT令牌
- **权限**: `vulnerability:manage`

**请求参数**:
```json
{
  "title": "SQL注入漏洞",
  "description": "登录页面存在SQL注入漏洞",
  "cve_id": "CVE-2024-1234",
  "cnvd_id": "CNVD-2024-1234",
  "owasp_category_id": 1,
  "severity_level": 2,
  "cvss_score": 7.5,
  "asset_id": 1,
  "assignee_id": 2,
  "impact_scope": "用户数据泄露",
  "reproduction_steps": "1. 访问登录页面\n2. 输入恶意SQL语句",
  "fix_suggestion": "使用参数化查询"
}
```

**参数验证**:
- `title`: 必填，1-255字符
- `severity_level`: 必填，枚举值：1-严重，2-高危，3-中危，4-低危
- `cvss_score`: 可选，0-10分
- `asset_id`: 必填，有效的资产ID

### 其他漏洞管理接口
- `GET /api/v1/vulnerabilities` - 获取漏洞列表（权限：vulnerability:read）
- `GET /api/v1/vulnerabilities/stats` - 获取漏洞统计（权限：vulnerability:read）
- `GET /api/v1/vulnerabilities/:id` - 获取漏洞详情（权限：vulnerability:read）
- `PUT /api/v1/vulnerabilities/:id` - 更新漏洞（权限：vulnerability:manage）
- `DELETE /api/v1/vulnerabilities/:id` - 删除漏洞（权限：vulnerability:manage）
- `PUT /api/v1/vulnerabilities/:id/status` - 更新漏洞状态（权限：vulnerability:manage）
- `POST /api/v1/vulnerabilities/:id/assign` - 分配漏洞（权限：vulnerability:assign）

---

## 📄 报告管理

### 上传报告
- **接口**: `POST /api/v1/reports`
- **描述**: 上传安全测试报告
- **认证**: 需要JWT令牌
- **权限**: `report:manage`
- **Content-Type**: `multipart/form-data`
- **速率限制**: 10次/分钟

**请求参数**:
- `file`: 报告文件（必填）
- `title`: 报告标题（必填）
- `description`: 报告描述
- `type`: 报告类型（必填）
- `severity`: 严重程度（必填）
- `asset_ids`: 关联资产ID列表
- `tags`: 标签列表
- `test_date`: 测试日期

**报告类型枚举**:
- `penetration_test`: 渗透测试
- `vulnerability_assessment`: 漏洞评估
- `security_audit`: 安全审计
- `compliance_check`: 合规检查
- `other`: 其他

### 其他报告管理接口
- `GET /api/v1/reports` - 获取报告列表（权限：report:read）
- `GET /api/v1/reports/stats` - 获取报告统计（权限：report:read）
- `GET /api/v1/reports/:id` - 获取报告详情（权限：report:read）
- `PUT /api/v1/reports/:id` - 更新报告（权限：report:manage）
- `DELETE /api/v1/reports/:id` - 删除报告（权限：report:manage）
- `GET /api/v1/reports/:id/download` - 下载报告（权限：report:read）

---

## 📊 统计分析

### 获取仪表板统计
- **接口**: `GET /api/v1/analytics/dashboard`
- **描述**: 获取仪表板统计数据
- **认证**: 需要JWT令牌
- **权限**: `analytics:read`

**响应数据**:
```json
{
  "total_assets": 150,
  "total_vulnerabilities": 89,
  "total_reports": 45,
  "total_users": 12,
  "severity_stats": [
    {"severity": 1, "count": 5},
    {"severity": 2, "count": 15}
  ],
  "recent_trends": {
    "vulnerability_trend": [
      {"date": "2025-08-01", "count": 5}
    ]
  }
}
```

### 获取漏洞趋势
- **接口**: `GET /api/v1/analytics/vulnerability/trend`
- **描述**: 获取漏洞趋势数据
- **认证**: 需要JWT令牌
- **权限**: `analytics:read`

**查询参数**:
- `start_date`: 开始日期
- `end_date`: 结束日期
- `granularity`: 时间粒度（day, week, month）

### 其他统计分析接口
- `GET /api/v1/analytics/overview` - 获取系统概览（权限：analytics:read）
- `GET /api/v1/analytics/vulnerability` - 获取漏洞分析（权限：analytics:read）
- `GET /api/v1/analytics/asset` - 获取资产分析（权限：analytics:read）
- `POST /api/v1/analytics/export` - 导出数据（权限：analytics:export）

---

## 🧪 API测试工具

### 测试API
- **接口**: `POST /api/v1/api-test/test`
- **描述**: 测试指定的API接口
- **认证**: 需要JWT令牌
- **权限**: `api:test`
- **速率限制**: 50次/秒
- **安全特性**: ✅ 已修复SSRF漏洞，添加URL白名单验证

**请求参数**:
```json
{
  "method": "GET",
  "url": "http://localhost:8080/api/v1/users",
  "headers": {
    "Authorization": "Bearer token",
    "Content-Type": "application/json"
  },
  "body": {},
  "timeout": 30
}
```

**URL安全限制**:
- 只允许HTTP和HTTPS协议
- 域名白名单：api.vulnark.com, test.vulnark.com, localhost, 127.0.0.1
- 端口白名单：80, 443, 8080, 8443, 3000, 8000
- 禁止访问内网地址和特殊地址

### 其他API测试接口
- `GET /api/v1/api-test/endpoints` - 获取API端点列表（权限：api:test）
- `POST /api/v1/api-test/token` - 生成API令牌（权限：api:test）
- `GET /api/v1/api-test/docs` - 获取API文档（权限：api:test）
- `POST /api/v1/api-test/validate` - 验证API请求（权限：api:test）

---

## 📖 使用示例

### 完整的API调用流程

#### 1. 用户登录
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 2. 获取用户列表
```bash
curl -X GET "http://localhost:8080/api/v1/users?page=1&page_size=10" \
  -H "Authorization: Bearer your-jwt-token"
```

#### 3. 创建资产
```bash
curl -X POST http://localhost:8080/api/v1/assets \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Web服务器01",
    "type": "server",
    "ip_address": "192.168.1.100",
    "importance_level": 1
  }'
```

#### 4. 上传报告
```bash
curl -X POST http://localhost:8080/api/v1/reports \
  -H "Authorization: Bearer your-jwt-token" \
  -F "file=@report.pdf" \
  -F "title=安全测试报告" \
  -F "type=penetration_test" \
  -F "severity=high"
```

---

## 🛡️ 安全使用指南

### 认证安全
1. **JWT令牌管理**
   - 妥善保管JWT令牌，避免泄露
   - 令牌过期时间为1小时，及时刷新
   - 不要在URL参数中传递令牌

2. **密码安全**
   - 使用强密码（至少6位）
   - 定期更换密码
   - 不要在代码中硬编码密码

### 权限安全
1. **最小权限原则**
   - 只分配必要的权限
   - 定期审查用户权限
   - 及时回收离职人员权限

2. **角色管理**
   - 合理设计角色权限
   - 避免权限过度集中
   - 建立权限审批流程

### API调用安全
1. **HTTPS使用**
   - 生产环境必须使用HTTPS
   - 验证SSL证书有效性
   - 避免中间人攻击

2. **输入验证**
   - 严格验证所有输入参数
   - 注意参数长度限制
   - 避免SQL注入和XSS攻击

3. **速率限制**
   - 遵守API速率限制
   - 实现客户端重试机制
   - 避免恶意请求

### 数据安全
1. **敏感数据处理**
   - 不要在日志中记录敏感信息
   - 使用HTTPS传输敏感数据
   - 定期备份重要数据

2. **文件上传安全**
   - 验证文件类型和大小
   - 扫描恶意文件
   - 隔离上传文件存储

---

## 📦 Postman集合

### 导入Postman集合

创建以下JSON文件并导入到Postman：

```json
{
  "info": {
    "name": "VulnArk API Collection",
    "description": "VulnArk漏洞管理系统API接口集合（安全版本）",
    "version": "1.0.0"
  },
  "auth": {
    "type": "bearer",
    "bearer": [{"key": "token", "value": "{{jwt_token}}"}]
  },
  "variable": [
    {"key": "base_url", "value": "http://localhost:8080"},
    {"key": "jwt_token", "value": ""}
  ],
  "item": [
    {
      "name": "认证",
      "item": [
        {
          "name": "用户登录",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
              "mode": "raw",
              "raw": "{\"username\": \"admin\", \"password\": \"admin123\"}"
            },
            "url": "{{base_url}}/api/v1/login"
          }
        }
      ]
    }
  ]
}
```

### 环境变量设置

在Postman中设置以下环境变量：
- `base_url`: http://localhost:8080
- `jwt_token`: 登录后获取的JWT令牌

---

## 🔧 开发工具

### Swagger文档
- **URL**: `http://localhost:8080/swagger/index.html`
- **描述**: 交互式API文档

### 安全测试工具
- **权限测试**: 使用不同角色的用户测试权限控制
- **速率限制测试**: 快速发送请求测试速率限制
- **参数验证测试**: 发送无效参数测试输入验证

---

## 📝 更新日志

### v1.0.0 (2025-08-12)

**安全修复**:
- ✅ 实现完整的权限中间件系统
- ✅ 修复API测试工具SSRF漏洞
- ✅ 添加API访问频率限制
- ✅ 加强分页参数限制（最大50条/页）
- ✅ 优化错误信息处理，避免敏感信息泄露
- ✅ 添加输入长度限制（搜索关键词最大100字符）

**功能改进**:
- 统一API文档格式
- 完善权限代码体系
- 增强安全验证机制
- 优化响应格式

**破坏性变更**:
- 所有管理接口现在需要相应权限
- 分页大小限制从100改为50
- API测试工具增加URL白名单限制

---

## 📞 联系信息

**技术支持**: support@vulnark.com  
**安全问题**: security@vulnark.com  
**API文档**: api-docs@vulnark.com

---

**文档版本**: v1.0.0  
**最后更新**: 2025-08-12  
**API版本**: v1.0.0  
**安全状态**: ✅ 已修复所有高危安全问题
