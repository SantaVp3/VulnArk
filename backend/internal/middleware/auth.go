package middleware

import (
	"strings"

	"github.com/gin-gonic/gin"
	"vulnark/pkg/auth"
	"vulnark/pkg/utils"
)

// AuthMiddleware JWT认证中间件
func AuthMiddleware() gin.HandlerFunc {
	jwtManager := auth.NewJWTManager()
	
	return func(c *gin.Context) {
		// 获取Authorization头
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			utils.UnauthorizedResponse(c, "缺少认证令牌")
			c.Abort()
			return
		}

		// 检查Bearer前缀
		if !strings.HasPrefix(authHeader, "Bearer ") {
			utils.UnauthorizedResponse(c, "认证令牌格式错误")
			c.Abort()
			return
		}

		// 提取令牌
		tokenString := strings.TrimPrefix(authHeader, "Bearer ")
		if tokenString == "" {
			utils.UnauthorizedResponse(c, "认证令牌为空")
			c.Abort()
			return
		}

		// 解析令牌
		claims, err := jwtManager.ParseToken(tokenString)
		if err != nil {
			utils.UnauthorizedResponse(c, "认证令牌无效: "+err.Error())
			c.Abort()
			return
		}

		// 将用户信息存储到上下文
		c.Set("user_id", claims.UserID)
		c.Set("username", claims.Username)
		c.Set("role_id", claims.RoleID)
		c.Set("claims", claims)

		c.Next()
	}
}

// OptionalAuthMiddleware 可选认证中间件（不强制要求认证）
func OptionalAuthMiddleware() gin.HandlerFunc {
	jwtManager := auth.NewJWTManager()
	
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader != "" && strings.HasPrefix(authHeader, "Bearer ") {
			tokenString := strings.TrimPrefix(authHeader, "Bearer ")
			if claims, err := jwtManager.ParseToken(tokenString); err == nil {
				c.Set("user_id", claims.UserID)
				c.Set("username", claims.Username)
				c.Set("role_id", claims.RoleID)
				c.Set("claims", claims)
			}
		}
		c.Next()
	}
}

// GetCurrentUserID 获取当前用户ID
func GetCurrentUserID(c *gin.Context) (uint, bool) {
	if userID, exists := c.Get("user_id"); exists {
		if id, ok := userID.(uint); ok {
			return id, true
		}
	}
	return 0, false
}

// GetCurrentUsername 获取当前用户名
func GetCurrentUsername(c *gin.Context) (string, bool) {
	if username, exists := c.Get("username"); exists {
		if name, ok := username.(string); ok {
			return name, true
		}
	}
	return "", false
}

// GetCurrentRoleID 获取当前用户角色ID
func GetCurrentRoleID(c *gin.Context) (uint, bool) {
	if roleID, exists := c.Get("role_id"); exists {
		if id, ok := roleID.(uint); ok {
			return id, true
		}
	}
	return 0, false
}

// GetCurrentClaims 获取当前用户Claims
func GetCurrentClaims(c *gin.Context) (*auth.Claims, bool) {
	if claims, exists := c.Get("claims"); exists {
		if c, ok := claims.(*auth.Claims); ok {
			return c, true
		}
	}
	return nil, false
}
