package middleware

import (
	"bytes"
	"io"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/auth"
)

// AuditMiddleware 审计日志中间件
func AuditMiddleware(auditService service.AuditLogService) gin.HandlerFunc {

	return func(c *gin.Context) {
		// 记录开始时间
		start := time.Now()

		// 读取请求体
		var requestBody string
		if c.Request.Body != nil {
			bodyBytes, _ := io.ReadAll(c.Request.Body)
			requestBody = string(bodyBytes)
			// 重新设置请求体，以便后续处理
			c.Request.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))
		}

		// 创建响应写入器包装器
		writer := &responseWriter{
			ResponseWriter: c.Writer,
			body:          &bytes.Buffer{},
		}
		c.Writer = writer

		// 处理请求
		c.Next()

		// 计算处理时间
		duration := time.Since(start)

		// 获取用户信息
		var userID *uint
		var username string
		if id, exists := GetCurrentUserID(c); exists {
			userID = &id
			// 从JWT token中获取用户名
			if claims, exists := c.Get("claims"); exists {
				if jwtClaims, ok := claims.(*auth.Claims); ok {
					username = jwtClaims.Username
				}
			}
		}

		// 确定操作动作和资源
		action, resource := determineActionAndResource(c.Request.Method, c.Request.URL.Path)

		// 获取响应体
		responseBody := writer.body.String()

		// 创建审计日志
		auditLog := &model.AuditLog{
			UserID:     userID,
			Username:   username,
			Action:     action,
			Resource:   resource,
			Method:     c.Request.Method,
			Path:       c.Request.URL.Path,
			IP:         c.ClientIP(),
			UserAgent:  c.Request.UserAgent(),
			Request:    requestBody,
			Response:   responseBody,
			Status:     c.Writer.Status(),
			Duration:   duration.Milliseconds(),
		}

		// 如果有错误，记录错误信息
		if len(c.Errors) > 0 {
			auditLog.Error = c.Errors.String()
		}

		// 异步保存审计日志
		go func() {
			auditService.CreateAuditLog(auditLog)
		}()
	}
}

// responseWriter 响应写入器包装器
type responseWriter struct {
	gin.ResponseWriter
	body *bytes.Buffer
}

func (w *responseWriter) Write(b []byte) (int, error) {
	w.body.Write(b)
	return w.ResponseWriter.Write(b)
}

func (w *responseWriter) WriteString(s string) (int, error) {
	w.body.WriteString(s)
	return w.ResponseWriter.WriteString(s)
}

// determineActionAndResource 确定操作动作和资源
func determineActionAndResource(method, path string) (string, string) {
	// 移除API前缀
	path = strings.TrimPrefix(path, "/api/v1")
	
	// 分割路径
	parts := strings.Split(strings.Trim(path, "/"), "/")
	if len(parts) == 0 {
		return "unknown", "unknown"
	}

	resource := parts[0]
	action := "unknown"

	// 根据HTTP方法和路径确定动作
	switch method {
	case "GET":
		if len(parts) == 1 {
			action = "list"
		} else {
			action = "read"
		}
	case "POST":
		if len(parts) > 1 {
			// 特殊操作
			switch parts[len(parts)-1] {
			case "login":
				action = "login"
			case "logout":
				action = "logout"
			case "assign":
				action = "assign"
			case "review":
				action = "review"
			case "submit":
				action = "submit"
			case "archive":
				action = "archive"
			case "import":
				action = "import"
			case "export":
				action = "export"
			default:
				action = "create"
			}
		} else {
			action = "create"
		}
	case "PUT":
		action = "update"
	case "DELETE":
		action = "delete"
	case "PATCH":
		action = "update"
	}

	// 特殊资源处理
	switch resource {
	case "login", "logout":
		return resource, "auth"
	case "profile":
		return "read", "profile"
	case "change-password":
		return "change_password", "user"
	case "public":
		if len(parts) > 1 {
			return "read", parts[1]
		}
		return "read", "public"
	}

	// 处理嵌套资源
	if len(parts) > 2 {
		// 例如: /users/1/roles -> resource: user_roles
		if parts[2] != "" && !isNumeric(parts[1]) {
			resource = parts[0] + "_" + parts[2]
		}
	}

	return action, resource
}

// isNumeric 检查字符串是否为数字
func isNumeric(s string) bool {
	for _, r := range s {
		if r < '0' || r > '9' {
			return false
		}
	}
	return true
}

// RequirePermission 权限检查中间件
func RequirePermission(permissionName string, permissionService service.PermissionService) gin.HandlerFunc {

	return func(c *gin.Context) {
		userID, exists := GetCurrentUserID(c)
		if !exists {
			c.JSON(401, gin.H{
				"code":    401,
				"message": "未登录",
			})
			c.Abort()
			return
		}

		hasPermission, err := permissionService.CheckUserPermission(userID, permissionName)
		if err != nil {
			c.JSON(500, gin.H{
				"code":    500,
				"message": "权限检查失败",
			})
			c.Abort()
			return
		}

		if !hasPermission {
			c.JSON(403, gin.H{
				"code":    403,
				"message": "权限不足",
			})
			c.Abort()
			return
		}

		c.Next()
	}
}

// AuditLogCleanupTask 审计日志清理任务
func AuditLogCleanupTask(auditService service.AuditLogService, configService service.SystemConfigService) {

	// 获取日志保留天数配置
	config, err := configService.GetConfigByKey("audit.log_retention_days")
	if err != nil {
		// 使用默认值90天
		auditService.CleanupOldLogs(90)
		return
	}

	days := config.Value.GetInt("value")
	if days <= 0 {
		days = 90 // 默认90天
	}

	auditService.CleanupOldLogs(days)
}
