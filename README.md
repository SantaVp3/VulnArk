# VulnArk - 漏洞管理系统

VulnArk 是一个现代化的企业级漏洞管理系统，基于 Go + Gin 框架开发，提供完整的漏洞生命周期管理、资产管理、报告生成、知识库等功能。

## 功能特性

### 核心功能

- **漏洞管理**
  - 漏洞录入与编辑（支持截图上传）
  - OWASP 分类支持
  - CVSS 评分管理
  - 漏洞状态流转（新建 → 已分配 → 处理中 → 已修复 → 已验证 → 已关闭）
  - 漏洞时间线追踪
  - 批量分配与自动分配规则

- **资产管理**
  - 资产信息录入与维护
  - 资产分类与重要性分级
  - 资产批量导入
  - 资产风险评估

- **用户与权限**
  - 用户管理（创建、编辑、删除）
  - 角色管理与权限分配
  - 基于角色的访问控制（RBAC）
  - JWT 身份认证
  - 双因素认证（2FA）支持

- **报告管理**
  - 报告上传与存储
  - 报告审核流程
  - 报告归档
  - 报告下载

- **知识库**
  - 知识文章管理
  - 标签分类
  - 模板管理
  - 全文搜索
  - 热门与相关文章推荐

- **通知系统**
  - 站内通知
  - 邮件通知
  - 邮件模板自定义
  - 通知设置

- **统计分析**
  - 仪表盘概览
  - 漏洞趋势分析
  - 资产风险评估
  - 用户活跃度统计
  - 数据导出

- **AI 助手**
  - 支持多种 AI 提供商（OpenAI、DeepSeek、通义千问等）
  - AI 对话管理
  - 用户可配置 AI 参数

- **系统管理**
  - 系统配置管理
  - 审计日志
  - 数据库优化
  - 缓存管理

## 技术栈

- **后端框架**: Go 1.23 + Gin
- **数据库**: MySQL / SQLite
- **认证**: JWT
- **API 文档**: Swagger
- **日志**: Logrus
- **配置管理**: Viper

## 项目结构

```
.
├── cmd/                    # 程序入口
│   └── main.go
├── configs/                # 配置文件
│   └── config.yaml
├── docs/                   # Swagger 文档
├── internal/               # 内部代码
│   ├── config/            # 配置加载
│   ├── container/         # 依赖注入容器
│   ├── controller/        # 控制器层
│   ├── middleware/        # 中间件
│   ├── model/             # 数据模型
│   ├── repository/        # 数据访问层
│   ├── router/            # 路由配置
│   └── service/           # 业务逻辑层
├── pkg/                    # 公共包
│   ├── auth/              # JWT 认证
│   ├── database/          # 数据库连接
│   ├── email/             # 邮件服务
│   ├── logger/            # 日志服务
│   ├── storage/           # 文件存储
│   └── utils/             # 工具函数
├── web/                    # 前端静态资源
└── go.mod
```

## 快速开始

### 环境要求

- Go 1.23+
- MySQL 5.7+ 或 SQLite3

### 安装

1. 克隆仓库

```bash
git clone https://github.com/SantaVp3/VulnArk.git
cd VulnArk
```

2. 安装依赖

```bash
go mod download
```

3. 配置数据库

编辑 `configs/config.yaml`，配置数据库连接信息：

```yaml
database:
  host: "localhost"
  port: 3306
  username: "root"
  password: "your_password"
  database: "vulnark"
  charset: "utf8mb4"
```

4. 运行程序

```bash
go run cmd/main.go
```

服务将在 `http://localhost:8080` 启动。

### 默认账户

首次启动时，系统会自动创建管理员账户：

- 用户名: `admin`
- 密码: `admin123`

> ⚠️ 请在首次登录后立即修改默认密码！

## API 文档

启动服务后，访问 Swagger 文档：

```
http://localhost:8080/swagger/index.html
```

## 配置说明

### 主要配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| server.port | 服务端口 | 8080 |
| server.mode | 运行模式 | release |
| database.host | 数据库地址 | localhost |
| database.port | 数据库端口 | 3306 |
| jwt.secret | JWT 密钥 | 请修改 |
| jwt.expire_time | Token 有效期(秒) | 3600 |
| storage.local_path | 文件存储路径 | ./uploads |

### 安全建议

1. 修改默认的 JWT 密钥
2. 生产环境设置 `server.mode` 为 `release`
3. 使用强密码保护数据库
4. 定期备份数据库
5. 配置 HTTPS

## API 端点概览

### 认证

- `POST /api/v1/login` - 用户登录

### 用户管理

- `GET /api/v1/users` - 获取用户列表
- `POST /api/v1/users` - 创建用户
- `GET /api/v1/users/:id` - 获取用户详情
- `PUT /api/v1/users/:id` - 更新用户
- `DELETE /api/v1/users/:id` - 删除用户

### 漏洞管理

- `GET /api/v1/vulnerabilities` - 获取漏洞列表
- `POST /api/v1/vulnerabilities` - 创建漏洞（支持截图上传）
- `GET /api/v1/vulnerabilities/:id` - 获取漏洞详情
- `PUT /api/v1/vulnerabilities/:id` - 更新漏洞
- `DELETE /api/v1/vulnerabilities/:id` - 删除漏洞
- `PUT /api/v1/vulnerabilities/:id/status` - 更新漏洞状态

### 资产管理

- `GET /api/v1/assets` - 获取资产列表
- `POST /api/v1/assets` - 创建资产
- `GET /api/v1/assets/:id` - 获取资产详情
- `PUT /api/v1/assets/:id` - 更新资产
- `DELETE /api/v1/assets/:id` - 删除资产

### 报告管理

- `GET /api/v1/reports` - 获取报告列表
- `POST /api/v1/reports` - 上传报告
- `GET /api/v1/reports/:id` - 获取报告详情
- `GET /api/v1/reports/:id/download` - 下载报告

### 知识库

- `GET /api/v1/knowledge` - 获取知识列表
- `POST /api/v1/knowledge` - 创建知识文章
- `GET /api/v1/knowledge/search` - 搜索知识库

### 统计分析

- `GET /api/v1/analytics/dashboard` - 仪表盘数据
- `GET /api/v1/analytics/vulnerability` - 漏洞分析
- `GET /api/v1/analytics/asset` - 资产分析

## 开发

### 生成 Swagger 文档

```bash
swag init -g cmd/main.go
```

### 运行测试

```bash
go test ./...
```

## 许可证

MIT License

## 联系方式

- GitHub: [SantaVp3/VulnArk](https://github.com/SantaVp3/VulnArk)
