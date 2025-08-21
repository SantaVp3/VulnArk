package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"vulnark/pkg/logger"
)

// LoggerMiddleware 日志中间件
func LoggerMiddleware() gin.HandlerFunc {
	return gin.LoggerWithFormatter(func(param gin.LogFormatterParams) string {
		logger.WithFields(map[string]interface{}{
			"status_code":  param.StatusCode,
			"latency":      param.Latency,
			"client_ip":    param.ClientIP,
			"method":       param.Method,
			"path":         param.Path,
			"error":        param.ErrorMessage,
			"user_agent":   param.Request.UserAgent(),
			"timestamp":    param.TimeStamp.Format(time.RFC3339),
		}).Info("HTTP请求")
		return ""
	})
}

// RequestLoggerMiddleware 请求日志中间件
func RequestLoggerMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		path := c.Request.URL.Path
		raw := c.Request.URL.RawQuery

		// 处理请求
		c.Next()

		// 记录日志
		latency := time.Since(start)
		clientIP := c.ClientIP()
		method := c.Request.Method
		statusCode := c.Writer.Status()

		if raw != "" {
			path = path + "?" + raw
		}

		logEntry := logger.WithFields(map[string]interface{}{
			"status_code": statusCode,
			"latency":     latency,
			"client_ip":   clientIP,
			"method":      method,
			"path":        path,
		})

		if len(c.Errors) > 0 {
			logEntry.Error(c.Errors.String())
		} else {
			logEntry.Info("请求处理完成")
		}
	}
}
