# VulnArk Deployment Guide

This guide covers how to deploy VulnArk in production environments.

## 🚀 Production Deployment

### Prerequisites
- Go 1.21+
- MySQL 8.0+
- Linux/Windows/macOS server

### Step 1: Prepare the Server

1. **Install Go:**
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install golang-go

   # CentOS/RHEL
   sudo yum install golang

   # Or download from https://golang.org/dl/
   ```

2. **Install MySQL:**
   ```bash
   # Ubuntu/Debian
   sudo apt install mysql-server

   # CentOS/RHEL
   sudo yum install mysql-server
   ```

3. **Create Database:**
   ```sql
   mysql -u root -p
   CREATE DATABASE vulnark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'vulnark'@'localhost' IDENTIFIED BY 'your_secure_password';
   GRANT ALL PRIVILEGES ON vulnark.* TO 'vulnark'@'localhost';
   FLUSH PRIVILEGES;
   ```

### Step 2: Deploy the Application

1. **Clone and Build:**
   ```bash
   git clone https://github.com/SantaVp3/VulnArk.git
   cd VulnArk
   cd backend/cmd
   go build -o vulnark main.go
   ```

2. **Configure:**
   ```bash
   # Edit configuration
   nano ../configs/config.yaml
   ```

3. **Run:**
   ```bash
   ./vulnark
   ```

### Step 3: Process Management (Systemd)

1. **Create Service File:**
   ```bash
   sudo nano /etc/systemd/system/vulnark.service
   ```

2. **Service Configuration:**
   ```ini
   [Unit]
   Description=VulnArk Vulnerability Management Platform
   After=network.target mysql.service

   [Service]
   Type=simple
   User=vulnark
   WorkingDirectory=/opt/vulnark/backend/cmd
   ExecStart=/opt/vulnark/backend/cmd/vulnark
   Restart=always
   RestartSec=5

   [Install]
   WantedBy=multi-user.target
   ```

3. **Enable and Start:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable vulnark
   sudo systemctl start vulnark
   sudo systemctl status vulnark
   ```

### Step 4: Reverse Proxy (Nginx)

1. **Install Nginx:**
   ```bash
   sudo apt install nginx
   ```

2. **Configure:**
   ```bash
   sudo nano /etc/nginx/sites-available/vulnark
   ```

3. **Nginx Configuration:**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```

4. **Enable Site:**
   ```bash
   sudo ln -s /etc/nginx/sites-available/vulnark /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

### Step 5: SSL Certificate (Let's Encrypt)

1. **Install Certbot:**
   ```bash
   sudo apt install certbot python3-certbot-nginx
   ```

2. **Get Certificate:**
   ```bash
   sudo certbot --nginx -d your-domain.com
   ```

## 🐳 Docker Deployment

### Dockerfile
```dockerfile
FROM golang:1.21-alpine AS builder

WORKDIR /app
COPY backend/ .
RUN go mod tidy
RUN go build -o vulnark cmd/main.go

FROM alpine:latest
RUN apk --no-cache add ca-certificates
WORKDIR /root/
COPY --from=builder /app/vulnark .
COPY --from=builder /app/configs ./configs
COPY --from=builder /app/web ./web
COPY database/ ./database/

EXPOSE 8080
CMD ["./vulnark"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  vulnark:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      - DB_HOST=mysql
      - DB_USER=vulnark
      - DB_PASSWORD=password
      - DB_NAME=vulnark

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: vulnark
      MYSQL_USER: vulnark
      MYSQL_PASSWORD: password
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 🔧 Configuration

### Environment Variables
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=vulnark
export DB_PASSWORD=your_password
export DB_NAME=vulnark
export JWT_SECRET=your_jwt_secret
export SERVER_PORT=8080
```

### Security Considerations
- Use strong database passwords
- Configure firewall rules
- Enable SSL/TLS
- Regular security updates
- Monitor logs
- Backup database regularly

## 📊 Monitoring

### Log Files
- Application logs: `/var/log/vulnark/`
- Nginx logs: `/var/log/nginx/`
- MySQL logs: `/var/log/mysql/`

### Health Check
```bash
curl http://localhost:8080/health
```

## 🔄 Updates

1. **Backup Database:**
   ```bash
   mysqldump -u vulnark -p vulnark > backup.sql
   ```

2. **Update Code:**
   ```bash
   git pull origin main
   cd backend/cmd
   go build -o vulnark main.go
   ```

3. **Restart Service:**
   ```bash
   sudo systemctl restart vulnark
   ```

## 🆘 Troubleshooting

### Common Issues
- **Port already in use**: Check if another service is using port 8080
- **Database connection failed**: Verify MySQL is running and credentials are correct
- **Permission denied**: Ensure proper file permissions and user ownership

### Logs
```bash
# Application logs
sudo journalctl -u vulnark -f

# Nginx logs
sudo tail -f /var/log/nginx/error.log
```
