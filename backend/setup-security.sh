#!/bin/bash

# VulnArk 安全配置脚本
# 用于生成安全的密钥和配置环境变量

set -e

echo "🔐 VulnArk 安全配置向导"
echo "========================"

# 检查 openssl 是否可用
if ! command -v openssl &> /dev/null; then
    echo "❌ 错误: 未找到 openssl 命令，请先安装 OpenSSL"
    exit 1
fi

# 生成 JWT 密钥
echo "🔑 生成 JWT 密钥..."
JWT_SECRET=$(openssl rand -base64 48)
echo "✅ JWT 密钥已生成"

# 生成随机数据库密码（如果需要）
echo "🔑 生成数据库密码..."
DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
echo "✅ 数据库密码已生成"

# 创建 .env 文件
ENV_FILE=".env"

if [ -f "$ENV_FILE" ]; then
    echo "⚠️  警告: $ENV_FILE 文件已存在"
    read -p "是否覆盖现有文件? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 操作已取消"
        exit 1
    fi
fi

echo "📝 创建 $ENV_FILE 文件..."

cat > "$ENV_FILE" << EOF
# VulnArk 环境变量配置
# 自动生成于 $(date)

# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/vulnark?useUnicode=true&characterEncoding=utf8&useSSL=true&requireSSL=true&serverTimezone=Asia/Shanghai
DB_USERNAME=vulnark
DB_PASSWORD=$DB_PASSWORD

# JWT 配置
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=86400000

# 扫描引擎配置
NUCLEI_PATH=/usr/local/bin/nuclei
NUCLEI_TEMPLATES_PATH=/opt/nuclei-templates
NUCLEI_OUTPUT_DIR=/tmp/nuclei-output

XRAY_PATH=/usr/local/bin/xray
XRAY_OUTPUT_DIR=/tmp/xray-output

# Nessus 配置（请手动填入实际值）
NESSUS_URL=https://localhost:8834
NESSUS_ACCESS_KEY=
NESSUS_SECRET_KEY=
NESSUS_USERNAME=
NESSUS_PASSWORD=

# AWVS 配置（请手动填入实际值）
AWVS_URL=https://localhost:3443
AWVS_API_KEY=
EOF

# 设置安全的文件权限
chmod 600 "$ENV_FILE"

echo "✅ $ENV_FILE 文件已创建"
echo "🔒 文件权限已设置为 600 (仅所有者可读写)"

echo ""
echo "🎉 安全配置完成！"
echo ""
echo "📋 下一步操作："
echo "1. 检查并修改 $ENV_FILE 中的数据库配置"
echo "2. 如需使用 Nessus 或 AWVS，请填入相应的配置信息"
echo "3. 确保 $ENV_FILE 文件不会被提交到版本控制系统"
echo "4. 启动应用程序"
echo ""
echo "🔐 安全提示："
echo "- 定期轮换 JWT 密钥和数据库密码"
echo "- 在生产环境中使用更强的密码策略"
echo "- 监控应用程序日志以发现异常活动"
echo ""
echo "生成的密钥信息："
echo "JWT Secret: $JWT_SECRET"
echo "DB Password: $DB_PASSWORD"
echo ""
echo "⚠️  请妥善保管这些密钥信息！"
