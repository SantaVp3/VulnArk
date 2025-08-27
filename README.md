# VulnArk - 漏洞管理平台

基于 Go 和 React 构建的漏洞管理平台，支持 AI 助手功能。

## 功能特性

- **漏洞管理**: 漏洞录入、编辑、分配和跟踪
- **资产管理**: IT 资产信息管理和分类
- **数据统计**: 基础的统计图表和数据展示
- **报告管理**: 报告文件上传和管理
- **知识库**: 安全知识文档管理
- **AI 助手**: 支持多种 AI 提供商（OpenAI、DeepSeek、通义千问）
- **用户管理**: 基于角色的权限控制
- **单体应用**: 后端集成前端静态文件

## 技术栈

- **后端**: Go + Gin + GORM + MySQL
- **前端**: React + TypeScript + Tailwind CSS（已编译）
- **数据库**: MySQL，支持自动迁移
- **认证**: JWT 令牌认证
- **AI 集成**: 多 AI 提供商支持

## 快速开始

### 环境要求
- Go 1.21+
- MySQL 8.0+

### 安装步骤

1. **克隆仓库:**
   ```bash
   git clone https://github.com/SantaVp3/VulnArk.git
   cd VulnArk
   ```

2. **创建 MySQL 数据库:**
   ```sql
   CREATE DATABASE vulnark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **配置数据库连接:**
   ```bash
   # 编辑 backend/configs/config.yaml
   # 更新数据库连接信息
   ```

4. **运行应用:**
   ```bash
   cd backend/cmd
   go run main.go
   ```

5. **访问应用:**
   - 打开 http://localhost:8080
   - 首次运行时控制台会显示管理员账号信息

## 配置说明

### 数据库配置
编辑 `backend/configs/config.yaml`:
```yaml
database:
  host: localhost
  port: 3306
  username: your_username
  password: your_password
  database: vulnark
```

### CORS 跨域配置
VulnArk 支持通过配置文件管理 CORS（跨域资源共享）设置，无需修改代码即可适应不同的部署环境。

#### 配置文件位置
- 主配置文件：`backend/configs/config.yaml`
- 开发环境模板：`backend/configs/config.dev.yaml`
- 生产环境模板：`backend/configs/config.prod.yaml`

#### CORS 配置示例
```yaml
cors:
  enabled: true                    # 是否启用CORS
  allowed_origins:                 # 允许的来源列表
    - "http://localhost:8080"
    - "https://your-domain.com"
  allowed_methods:                 # 允许的HTTP方法
    - "GET"
    - "POST"
    - "PUT"
    - "DELETE"
  allowed_headers:                 # 允许的请求头
    - "Origin"
    - "Content-Type"
    - "Authorization"
  exposed_headers:                 # 暴露给客户端的响应头
    - "Content-Length"
    - "Authorization"
  allow_credentials: true          # 是否允许携带凭据
  max_age: 43200                  # 预检请求缓存时间（秒）
```

#### 环境配置
**开发环境:**
```yaml
cors:
  enabled: true
  allowed_origins:
    - "http://localhost:3000"    # React开发服务器
    - "http://localhost:5173"    # Vite开发服务器
    - "http://localhost:8080"    # 本地后端
  max_age: 3600                  # 1小时缓存
```

**生产环境:**
```yaml
cors:
  enabled: true
  allowed_origins:
    - "https://your-domain.com"      # 生产域名
    - "http://10.211.55.6:8080"      # 服务器IP（如需要）
  max_age: 86400                     # 24小时缓存
```

#### 安全最佳实践
- ❌ 避免使用通配符 `"*"` 允许所有来源
- ✅ 只配置应用实际需要的域名、HTTP方法和请求头
- ✅ 生产环境使用HTTPS域名和较长的缓存时间

### AI 助手配置
通过 Web 界面配置 AI 提供商:
1. 使用管理员账号登录
2. 进入 设置 → AI 助手
3. 配置您选择的 AI 提供商
4. 测试连接

## 开发说明

### 开发环境运行
```bash
cd backend/cmd
go run main.go
```

### 生产环境构建
```bash
cd backend/cmd
go build -o vulnark main.go
./vulnark
```

## 项目结构

```
VulnArk/
├── backend/
│   ├── cmd/main.go              # 应用程序入口
│   ├── internal/                # 业务逻辑
│   ├── pkg/                     # 工具包
│   ├── configs/                 # 配置文件
│   └── web/                     # 编译后的前端文件
├── database/
│   ├── migrations/              # 数据库迁移文件
│   └── seeds/                   # 初始数据
└── README.md
```

## 主要功能

### 自动化设置
- **数据库迁移**: 自动创建数据库表结构
- **初始数据**: 预置示例数据
- **管理员账号**: 首次运行时自动生成随机密码

### 安全特性
- JWT 令牌认证
- 基于角色的权限控制
- 输入数据验证
- SQL 注入防护

### AI 集成
- 多 AI 提供商支持
- 对话记录管理
- 使用统计
- 简单配置

## 故障排除

### 常见问题

#### 1. 权限不足错误
**症状**: 管理员登录后无法访问漏洞管理等功能
**解决方案**:
- 确认管理员角色有正确的权限配置
- 检查数据库中 `roles` 表的 `permissions` 字段
- 管理员应该有 `["*"]` 通配符权限或具体的功能权限

#### 2. CORS 跨域错误
**症状**: 前端页面显示但API请求失败，浏览器控制台显示CORS错误
**解决方案**:
- 检查 `config.yaml` 中的 CORS 配置
- 确认 `allowed_origins` 包含客户端访问的域名
- 重启服务器使配置生效

#### 3. 连接被拒绝
**症状**: `net::ERR_CONNECTION_REFUSED`
**解决方案**:
- 确认服务器正在运行: `ps aux | grep vulnark`
- 检查端口是否正确监听: `netstat -tlnp | grep 8080`
- 确认防火墙设置允许8080端口访问

#### 4. 前端页面无法加载
**症状**: 访问服务器IP时页面无法显示
**解决方案**:
- 检查服务器配置中的 `host` 设置为 `""` 或 `"0.0.0.0"`
- 确认前端文件已正确编译到 `backend/web/` 目录
- 检查静态文件路由配置

### API 测试命令
```bash
# 测试服务器连通性
curl http://your-server:8080/api/v1/public/ping

# 测试登录接口
curl -X POST http://your-server:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your_password"}'

# 测试权限检查
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "http://your-server:8080/api/v1/permissions/check?permission=vuln:read"
```

## 贡献指南

1. Fork 本仓库
2. 创建功能分支
3. 提交您的更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。

## 作者

**SantaVp3**
- 邮箱: VpSanta3@gmail.com
- GitHub: [@SantaVp3](https://github.com/SantaVp3)

---

**VulnArk** - 简单实用的漏洞管理平台
