# VulnArk 漏洞管理平台

> **⚠️ 当前版本为测试版本，必然存在许多Bug，如有遇到请提交 [Issue](https://github.com/SantaVp3/VulnArk/issues)。**

VulnArk 是一个漏洞管理和安全扫描平台，提供资产管理、漏洞扫描、基线检查等功能。

## 技术栈

- **前端**：Vue 3 + TypeScript + Arco Design UI + ECharts
- **后端**：Spring Boot 3.2 + Spring Security + Spring Data JPA
- **数据库**：MySQL 8.0
- **认证**：JWT Token
- **Agent**：Go语言跨平台客户端

## 功能模块

### 用户管理
- 用户登录认证（JWT）
- 用户信息管理
- 角色权限控制

### 资产管理
- 资产增删改查
- 资产分类管理（服务器、工作站、网络设备等）
- 资产状态跟踪（活跃、非活跃、维护中、已退役）
- 资产重要性评级

### 漏洞管理
- 漏洞信息管理
- 漏洞严重程度分级（信息、低、中、高、严重）
- **丰富的验证状态**：已发现、待修复、修复中、待复测、复测中、已修复、已验证、误报、风险接受、不予修复
- 漏洞状态跟踪和工作流管理
- 漏洞分配与处理
- 按验证状态和资产筛选查询

### 扫描管理
- 漏洞扫描任务创建
- 扫描结果查看
- 扫描工具管理（Xray、Nuclei）
- 扫描配置管理

### 基线检查
- Agent模式基线扫描
- 跨平台Agent客户端（Windows/Linux）
- 基线检查规则管理
- 基线扫描任务调度
- 检查结果统计分析

### 数据统计
- 仪表盘数据展示
- 漏洞趋势分析
- 资产状态统计
- 黄道吉日功能（网络安全主题）

## 主要特性

- **Agent架构**：支持跨平台Agent客户端进行基线检查
- **多扫描引擎**：集成Xray、Nuclei等主流扫描工具
- **实时监控**：Agent心跳监控和任务状态跟踪
- **规则引擎**：可配置的基线检查规则系统
- **响应式界面**：基于Arco Design的现代化UI
- **安全认证**：JWT token认证和权限控制

## 版本说明

**当前版本**：v1.1.0-beta（测试版）

**版本特点**：
- 功能基本完整，但仍在开发完善中
- 必然存在各种Bug和不完善之处
- 适合学习、测试和功能验证
- 欢迎反馈问题和建议

### 🆕 v1.1.0 新增功能
- **漏洞验证状态丰富化**：从3个状态扩展到10个状态，支持完整的漏洞处理工作流
- **API路径标准化**：统一所有后端接口路径，添加`/api`前缀
- **Dashboard优化**：修复数据加载问题，移除外部依赖的功能模块
- **资产管理增强**：完善资产分页查询和批量操作功能
- **前后端集成优化**：修复多个API调用问题，提升系统稳定性
- **数据库架构更新**：支持新的验证状态字段和查询优化

**遇到问题？**
- 请在 [GitHub Issues](https://github.com/SantaVp3/VulnArk/issues) 中提交Bug报告
- 描述问题时请包含：操作步骤、错误信息、环境信息
- 我们会尽快处理和回复

## 环境要求

- Docker 20.10+
- Docker Compose 2.0+

## 🚀 一键部署（推荐）

### 快速启动

```bash
# 克隆项目
git clone https://github.com/SantaVp3/VulnArk.git
cd VulnArk

# 一键启动
./start.sh
```

启动完成后访问：
- **前端界面**：http://localhost
- **后端API**：http://localhost:8080/api
- **API文档**：http://localhost:8080/api/swagger-ui.html

### 默认登录信息

```
用户名：admin
密码：password123
```

### 常用命令

```bash
# 查看服务状态
docker compose ps
# 或者（旧版本）
docker-compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down

# 重启服务
docker compose restart

# 清理数据（谨慎使用）
docker compose down -v
```

## 📦 手动部署

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 后端部署

```bash
cd backend
mvn clean package -DskipTests
java -jar target/vulnark-backend-1.0.0.jar
```

### 前端部署

```bash
cd frontend
npm install
npm run build
# 使用nginx或其他web服务器托管dist目录
```

## 🔒 安全注意事项

> **重要提醒：本项目为测试版本，使用默认配置，仅供学习和测试使用。**

### 测试环境配置
- **默认数据库密码**：vulnark123
- **默认JWT密钥**：vulnark-test-secret-key-2024
- **默认管理员账号**：admin/password123

### 生产环境部署建议
1. **修改默认配置**：
   - 更改数据库密码
   - 生成新的JWT密钥
   - 修改管理员密码

2. **网络安全**：
   - 配置防火墙规则
   - 使用HTTPS
   - 限制数据库访问

3. **数据安全**：
   - 定期备份数据
   - 配置数据库SSL
   - 设置访问日志

## 🔧 故障排除

### 常见问题

**1. 容器启动失败**
```bash
# 查看容器状态
docker compose ps

# 查看错误日志
docker compose logs backend
docker compose logs frontend
```

**2. 端口占用**
```bash
# 检查端口占用
lsof -i :80
lsof -i :8080

# 修改端口（编辑docker-compose.yml）
ports:
  - "8081:8080"  # 修改为其他端口
```

**3. 数据库连接问题**
```bash
# 重启数据库容器
docker compose restart mysql

# 检查数据库日志
docker compose logs mysql
```

**4. 前端无法访问后端**
```bash
# 检查网络连接
docker network ls
docker compose logs frontend
```

### Bug反馈

遇到问题请提交 [GitHub Issues](https://github.com/SantaVp3/VulnArk/issues)，包含：
- 操作步骤和错误信息
- 系统环境（操作系统、Docker版本）
- 相关日志输出

## 项目截图
<img width="779" alt="iShot_2025-07-03_15 25 01" src="https://github.com/user-attachments/assets/f1a0d26e-ef5c-40fb-9214-1d27798e7e0b" />
<img width="1473" alt="iShot_2025-07-03_15 25 38" src="https://github.com/user-attachments/assets/e21add73-679d-42f0-93b1-d193c695e899" />
<img width="1474" alt="iShot_2025-07-03_15 25 32" src="https://github.com/user-attachments/assets/bd3a862b-a523-4a5a-9432-bcf435027e3b" />
<img width="1467" alt="iShot_2025-07-03_15 25 25" src="https://github.com/user-attachments/assets/039099ca-ca75-41e7-9717-a765187186d1" />

## 项目结构

```
vulnark+/
├── backend/                # 后端服务
│   ├── src/main/java/com/vulnark/
│   │   ├── controller/     # REST API控制器
│   │   ├── service/        # 业务逻辑层
│   │   ├── repository/     # 数据访问层
│   │   ├── entity/         # JPA实体类
│   │   ├── dto/            # 数据传输对象
│   │   ├── config/         # 配置类
│   │   └── security/       # 安全认证
│   └── src/main/resources/ # 配置文件
├── frontend/               # 前端界面
│   ├── src/
│   │   ├── api/            # API接口调用
│   │   ├── views/          # 页面组件
│   │   ├── components/     # 公共组件
│   │   ├── stores/         # 状态管理
│   │   └── router/         # 路由配置
│   └── public/             # 静态资源
└── agent/                  # Agent客户端（Go）
    ├── main.go             # 主程序入口
    ├── config/             # 配置管理
    ├── baseline/           # 基线检查引擎
    ├── communication/      # 服务端通信
    └── utils/              # 工具函数
```

## 使用说明

**测试环境快速体验**：

1. 使用默认账号（admin/password123）登录系统
2. 在仪表盘查看系统概览和黄道吉日功能
3. 在资产管理中添加测试资产信息
4. 在扫描管理中体验扫描功能（需要配置扫描工具）
5. 在基线检查中测试Agent模式功能
6. 在漏洞管理中查看和管理发现的问题

**注意**：当前为测试版本，数据仅供演示使用。

## 反馈与联系

**Bug反馈**：[GitHub Issues](https://github.com/SantaVp3/VulnArk/issues) （推荐）
**项目地址**：https://github.com/SantaVp3/VulnArk.git
**邮箱联系**：vpsanta3@gmail.com

> 💡 建议优先通过GitHub Issues反馈问题，这样可以更好地跟踪和解决Bug。

## 许可证

[MIT License](LICENSE)
