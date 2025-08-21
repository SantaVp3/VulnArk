package middleware

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"vulnark/pkg/logger"
	"vulnark/pkg/utils"
)

// RecoveryMiddleware 恢复中间件
func RecoveryMiddleware() gin.HandlerFunc {
	return gin.CustomRecovery(func(c *gin.Context, recovered interface{}) {
		logger.WithFields(map[string]interface{}{
			"error":      recovered,
			"path":       c.Request.URL.Path,
			"method":     c.Request.Method,
			"client_ip":  c.ClientIP(),
			"user_agent": c.Request.UserAgent(),
		}).Error("服务器内部错误")

		utils.ErrorResponse(c, http.StatusInternalServerError, "服务器内部错误")
	})
}
