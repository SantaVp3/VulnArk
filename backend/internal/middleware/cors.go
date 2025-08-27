package middleware

import (
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"vulnark/internal/config"
)

// CORSMiddleware CORS中间件 - 从配置文件读取设置
func CORSMiddleware() gin.HandlerFunc {
	corsConfig := config.AppConfig.CORS

	// 如果CORS未启用，返回空中间件
	if !corsConfig.Enabled {
		return gin.HandlerFunc(func(c *gin.Context) {
			c.Next()
		})
	}

	// 设置默认值（防止配置文件缺少某些字段）
	allowedOrigins := corsConfig.AllowedOrigins
	if len(allowedOrigins) == 0 {
		allowedOrigins = []string{"http://localhost:8080"}
	}

	allowedMethods := corsConfig.AllowedMethods
	if len(allowedMethods) == 0 {
		allowedMethods = []string{"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"}
	}

	allowedHeaders := corsConfig.AllowedHeaders
	if len(allowedHeaders) == 0 {
		allowedHeaders = []string{"Origin", "Content-Length", "Content-Type", "Authorization", "X-Requested-With", "Accept"}
	}

	exposedHeaders := corsConfig.ExposedHeaders
	if len(exposedHeaders) == 0 {
		exposedHeaders = []string{"Content-Length", "Content-Type", "Authorization"}
	}

	maxAge := corsConfig.MaxAge
	if maxAge == 0 {
		maxAge = 43200 // 12小时默认值
	}

	return cors.New(cors.Config{
		AllowOrigins:     allowedOrigins,
		AllowMethods:     allowedMethods,
		AllowHeaders:     allowedHeaders,
		ExposeHeaders:    exposedHeaders,
		AllowCredentials: corsConfig.AllowCredentials,
		MaxAge:           time.Duration(maxAge) * time.Second,
	})
}
