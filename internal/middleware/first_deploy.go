package middleware

import (
	"os"
	"github.com/gin-gonic/gin"
)

// FirstDeployMiddleware 首次部署中间件
// 用于在响应头中添加首次部署标识
func FirstDeployMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// 检查是否存在管理员凭证文件
		if _, err := os.Stat("admin_credentials.txt"); err == nil {
			c.Header("X-First-Deploy", "true")
		} else {
			c.Header("X-First-Deploy", "false")
		}
		
		c.Next()
	}
}