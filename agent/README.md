# VulnArk Agent

VulnArk Agent 是一个轻量级的跨平台基线扫描代理程序，用于在目标系统上执行安全基线检查。

## 特性

- 🔄 **跨平台支持**: 支持 Windows、Linux、macOS
- 🛡️ **安全通信**: 使用 JWT Token 进行身份认证
- 📊 **实时监控**: 心跳机制确保连接状态
- 🔍 **基线检查**: 支持 CIS Benchmarks 等安全标准
- 📝 **详细日志**: 完整的操作日志记录
- ⚙️ **灵活配置**: YAML 配置文件，易于管理

## 系统要求

### Windows
- Windows 7/Server 2008 R2 或更高版本
- 管理员权限（用于系统级检查）

### Linux
- 内核版本 2.6.32 或更高
- Root 权限（用于系统级检查）

### macOS
- macOS 10.12 或更高版本
- 管理员权限

## 快速开始

### 1. 下载和安装

从 [Releases](releases) 页面下载适合您系统的版本：

```bash
# Linux
wget https://github.com/your-repo/vulnark-agent/releases/download/v1.0.0/vulnark-agent-1.0.0-linux-amd64.tar.gz
tar -xzf vulnark-agent-1.0.0-linux-amd64.tar.gz
cd vulnark-agent-1.0.0-linux-amd64

# Windows (PowerShell)
Invoke-WebRequest -Uri "https://github.com/your-repo/vulnark-agent/releases/download/v1.0.0/vulnark-agent-1.0.0-windows-amd64.zip" -OutFile "vulnark-agent.zip"
Expand-Archive vulnark-agent.zip
cd vulnark-agent-1.0.0-windows-amd64
```

### 2. 配置

编辑 `config.yaml` 文件：

```yaml
server:
  url: "http://your-vulnark-server:8080"  # VulnArk 服务器地址
  
agent:
  name: "my-agent"  # Agent 名称
  description: "Production Server Agent"
  
logging:
  level: "info"
  file: "/var/log/vulnark/agent.log"  # 日志文件路径
```

### 3. 运行

```bash
# Linux
sudo ./vulnark-agent

# Windows (以管理员身份运行)
vulnark-agent.exe
```

## 配置说明

### 完整配置示例

```yaml
server:
  url: "http://localhost:8080"           # 服务器地址
  timeout: 30                            # 请求超时时间（秒）
  heartbeat_interval: 30                 # 心跳间隔（秒）
  task_poll_interval: 60                 # 任务轮询间隔（秒）
  retry_count: 3                         # 重试次数
  retry_delay: 5                         # 重试延迟（秒）

agent:
  name: "vulnark-agent-hostname"         # Agent 名称
  description: "VulnArk Baseline Scanner Agent"
  work_dir: "/var/lib/vulnark/agent"     # 工作目录
  log_dir: "/var/log/vulnark/agent"      # 日志目录
  temp_dir: "/tmp/vulnark-agent"         # 临时目录
  max_tasks: 5                           # 最大并发任务数

security:
  agent_id: ""                           # Agent ID（自动生成）
  token: ""                              # 认证 Token（自动生成）
  tls_verify: true                       # TLS 证书验证
  cert_file: ""                          # 客户端证书文件
  key_file: ""                           # 客户端私钥文件
  ca_file: ""                            # CA 证书文件

logging:
  level: "info"                          # 日志级别：debug, info, warn, error
  file: ""                               # 日志文件路径（空则输出到控制台）
  max_size: 100                          # 日志文件最大大小（MB）
  max_backups: 3                         # 保留的日志文件数量
  max_age: 28                            # 日志文件保留天数
  compress: true                         # 是否压缩旧日志文件
```

## 命令行选项

```bash
vulnark-agent [选项]

选项:
  -config string    配置文件路径
  -info            显示系统信息
  -status          显示 Agent 状态
  -version         显示版本信息
  -daemon          以守护进程模式运行
  -h               显示帮助信息
```

### 示例

```bash
# 显示系统信息
./vulnark-agent -info

# 使用自定义配置文件
./vulnark-agent -config /etc/vulnark/agent.yaml

# 以守护进程模式运行
./vulnark-agent -daemon

# 显示 Agent 状态
./vulnark-agent -status
```

## 服务安装

### Linux (systemd)

使用提供的安装脚本：

```bash
sudo ./install-systemd.sh
```

或手动安装：

```bash
# 复制文件到系统目录
sudo cp vulnark-agent /usr/local/bin/
sudo cp config.yaml /etc/vulnark/agent.yaml

# 创建 systemd 服务文件
sudo tee /etc/systemd/system/vulnark-agent.service > /dev/null << EOF
[Unit]
Description=VulnArk Baseline Scanner Agent
After=network.target

[Service]
Type=simple
User=root
ExecStart=/usr/local/bin/vulnark-agent -config /etc/vulnark/agent.yaml
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启用并启动服务
sudo systemctl daemon-reload
sudo systemctl enable vulnark-agent
sudo systemctl start vulnark-agent
```

### Windows 服务

使用提供的安装脚本：

```cmd
install-service.bat
```

或使用 sc 命令：

```cmd
sc create VulnArkAgent binPath= "C:\path\to\vulnark-agent.exe -daemon" start= auto
sc description VulnArkAgent "VulnArk Baseline Scanner Agent"
sc start VulnArkAgent
```

## 开发构建

### 环境要求

- Go 1.21 或更高版本
- Git

### 构建步骤

```bash
# 克隆代码
git clone https://github.com/your-repo/vulnark-agent.git
cd vulnark-agent

# 安装依赖
go mod tidy

# 构建当前平台版本
go build -o vulnark-agent main.go

# 构建所有平台版本
chmod +x build.sh
./build.sh
```

### 交叉编译

```bash
# Windows amd64
GOOS=windows GOARCH=amd64 go build -o vulnark-agent-windows-amd64.exe main.go

# Linux amd64
GOOS=linux GOARCH=amd64 go build -o vulnark-agent-linux-amd64 main.go

# macOS amd64
GOOS=darwin GOARCH=amd64 go build -o vulnark-agent-darwin-amd64 main.go
```

## 故障排除

### 常见问题

1. **连接服务器失败**
   - 检查服务器地址和端口是否正确
   - 确认网络连接正常
   - 检查防火墙设置

2. **权限不足**
   - 确保以管理员/root 权限运行
   - 检查文件和目录权限

3. **注册失败**
   - 检查服务器是否正常运行
   - 确认服务器配置允许 Agent 注册

### 日志分析

查看详细日志：

```bash
# Linux
tail -f /var/log/vulnark/agent.log

# Windows
type C:\ProgramData\VulnArk\agent\logs\agent.log
```

### 调试模式

启用调试日志：

```yaml
logging:
  level: "debug"
```

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 支持

如有问题，请：

1. 查看 [FAQ](docs/FAQ.md)
2. 搜索 [Issues](issues)
3. 提交新的 [Issue](issues/new)
