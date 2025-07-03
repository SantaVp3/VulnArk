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

### 后端启动
```bash
cd backend
./mvnw spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 登录
```bash
username：admin
Password：password123

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
