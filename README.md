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
