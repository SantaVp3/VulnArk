# VulnArk+ 漏洞管理平台

VulnArk+ 是一个专注于漏洞管理和资产安全的综合平台，旨在帮助企业和组织有效地管理、跟踪和修复安全漏洞。

## 技术栈

- **前端**：Vue 3 + TypeScript + Arco Design UI + Echarts
- **后端**：Spring Boot 3.2 + Spring Security + Spring Data JPA
- **数据库**：MySQL 8.0
- **认证**：JWT Token
- **API文档**：SpringDoc OpenAPI

## 已实现功能

### 用户认证与管理
- 用户登录与注册
- 基于JWT的认证授权
- 用户权限管理（管理员、项目经理、安全分析师、普通用户）
- 用户信息管理与密码重置

### 资产管理
- 资产创建、编辑、删除和查询
- 资产分类（服务器、工作站、网络设备、数据库等）
- 资产重要性评级（低、中、高、关键）
- 资产状态管理（活跃、非活跃、维护中、已退役）
- 资产详情查看与批量操作

### 漏洞管理
- 漏洞创建、编辑、删除和查询
- 漏洞严重程度分级（信息、低危、中危、高危、严重）
- 漏洞状态跟踪（待处理、处理中、已解决、已关闭、重新打开）
- 漏洞分配与责任人管理
- 漏洞批量操作（批量分配、批量删除）
- 漏洞详情与CVE关联

### 项目管理
- 项目创建与管理
- 项目成员分配
- 项目资产关联

### 数据统计与可视化
- 仪表盘概览
- 漏洞统计与趋势分析
- 资产安全状态统计

## 计划中功能

### 扫描管理
- 安全扫描任务创建与执行
- 扫描结果分析与导入
- 扫描模板管理

### 资产发现
- 网络资产自动发现
- 资产指纹识别
- 资产变更监控

### 资产依赖分析
- 资产依赖关系可视化
- 依赖风险分析
- 影响路径分析

### 基线检查
- 安全基线合规检查
- 基线检查项管理
- 合规报告生成

## 安装与使用

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

## 🚀 快速部署指南

### 1. 数据库准备

创建MySQL数据库：
```sql
CREATE DATABASE vulnark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'vulnark'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON vulnark.* TO 'vulnark'@'%';
FLUSH PRIVILEGES;
```

### 2. 后端部署

#### 2.1 安全配置（重要）

首先运行安全配置脚本生成安全密钥：
```bash
cd backend
chmod +x setup-security.sh
./setup-security.sh
```

#### 2.2 环境变量配置

编辑生成的 `.env` 文件，配置数据库连接：
```bash
# 数据库配置
DB_URL=jdbc:mysql://your-host:3306/vulnark?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
DB_USERNAME=vulnark
DB_PASSWORD=your_password

# JWT配置（已自动生成安全密钥）
JWT_SECRET=your-generated-jwt-secret
JWT_EXPIRATION=86400000
```

#### 2.3 启动后端服务

**方式一：使用环境变量启动（推荐）**
```bash
cd backend
export $(cat .env | xargs) && mvn spring-boot:run
```

**方式二：直接设置环境变量**
```bash
cd backend
export JWT_SECRET="your-jwt-secret"
export DB_PASSWORD="your-db-password"
export DB_USERNAME="vulnark"
export DB_URL="jdbc:mysql://your-host:3306/vulnark?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
mvn spring-boot:run
```

**方式三：生产环境部署**
```bash
cd backend
mvn clean package -DskipTests
java -jar target/vulnark-backend-1.0.0.jar
```

#### 2.4 验证后端启动

后端启动成功后，访问以下地址验证：
- 应用地址：http://localhost:8080/api
- API文档：http://localhost:8080/api/swagger-ui.html
- 健康检查：http://localhost:8080/api/actuator/health

### 3. 前端部署

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问：http://localhost:3000

### 4. 默认登录信息

```
用户名：admin
密码：password123
```

## 🔒 安全注意事项

1. **生产环境部署前必须**：
   - 修改默认管理员密码
   - 使用强随机JWT密钥
   - 配置数据库SSL连接
   - 设置防火墙规则

2. **敏感信息保护**：
   - 不要将 `.env` 文件提交到版本控制
   - 定期轮换JWT密钥和数据库密码
   - 使用环境变量管理敏感配置

3. **安全功能**：
   - 已修复RCE漏洞，使用安全的命令执行器
   - JWT密钥验证和安全检查
   - 命令白名单和危险模式检测

## 🔧 故障排除

### 常见问题

**1. JWT密钥错误**
```
错误：JWT密钥未配置！请设置环境变量 JWT_SECRET
解决：运行 ./setup-security.sh 生成安全密钥
```

**2. 数据库连接失败**
```
错误：Access denied for user 'vulnark'@'localhost'
解决：检查数据库用户权限和密码配置
```

**3. 端口占用**
```
错误：Port 8080 was already in use
解决：修改 application.yml 中的 server.port 配置
```

**4. 默认用户登录失败**
```
确认：用户名 admin，密码 password123
检查：数据库中 users 表是否有默认用户记录
```

### 日志查看

查看应用日志：
```bash
# 开发环境
tail -f logs/vulnark.log

# 生产环境
journalctl -u vulnark -f
```

## 项目截图
<img width="779" alt="iShot_2025-07-03_15 25 01" src="https://github.com/user-attachments/assets/f1a0d26e-ef5c-40fb-9214-1d27798e7e0b" />
<img width="1473" alt="iShot_2025-07-03_15 25 38" src="https://github.com/user-attachments/assets/e21add73-679d-42f0-93b1-d193c695e899" />
<img width="1474" alt="iShot_2025-07-03_15 25 32" src="https://github.com/user-attachments/assets/bd3a862b-a523-4a5a-9432-bcf435027e3b" />
<img width="1467" alt="iShot_2025-07-03_15 25 25" src="https://github.com/user-attachments/assets/039099ca-ca75-41e7-9717-a765187186d1" />

## 项目结构

```
vulnark+/
├── backend/                # 后端代码
│   ├── src/                # 源代码
│   │   ├── main/java/com/vulnark/
│   │   │   ├── controller/ # 控制器
│   │   │   ├── service/    # 业务逻辑
│   │   │   ├── repository/ # 数据访问
│   │   │   ├── entity/     # 实体类
│   │   │   ├── dto/        # 数据传输对象
│   │   │   ├── config/     # 配置类
│   │   │   └── security/   # 安全相关
│   │   └── resources/      # 配置文件
│   └── pom.xml             # Maven配置
└── frontend/               # 前端代码
    ├── src/
    │   ├── api/            # API调用
    │   ├── views/          # 页面组件
    │   ├── components/     # 通用组件
    │   ├── stores/         # 状态管理
    │   └── router/         # 路由配置
    ├── package.json        # NPM配置
    └── vite.config.ts      # Vite配置
```

## 开发团队

VulnArk+ 由安全开发团队开发和维护。

## 联系作者
 
vpsanta3@gmail.com

## 许可证

[MIT License](LICENSE)
