package utils

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"vulnark/internal/model"
)

// SuccessResponse 成功响应
func SuccessResponse(c *gin.Context, data interface{}) {
	c.JSON(http.StatusOK, model.NewSuccessResponse(data))
}

// sanitizeErrorMessage 净化错误信息，避免敏感信息泄露
func sanitizeErrorMessage(message string) string {
	// 敏感信息关键词
	sensitiveKeywords := []string{
		"password", "token", "secret", "key", "database", "sql", "connection",
		"internal", "system", "config", "env", "path", "file", "directory",
	}

	lowerMessage := strings.ToLower(message)

	// 检查是否包含敏感信息
	for _, keyword := range sensitiveKeywords {
		if strings.Contains(lowerMessage, keyword) {
			return "请求处理失败，请检查输入参数"
		}
	}

	// 移除可能的路径信息
	if strings.Contains(message, "/") || strings.Contains(message, "\\") {
		return "请求处理失败，请检查输入参数"
	}

	// 限制错误信息长度
	if len(message) > 100 {
		return "请求处理失败，请检查输入参数"
	}

	return message
}

// ErrorResponse 错误响应
func ErrorResponse(c *gin.Context, code int, message string) {
	safeMessage := sanitizeErrorMessage(message)
	c.JSON(code, model.NewErrorResponse(code, safeMessage))
}

// BadRequestResponse 请求参数错误响应
func BadRequestResponse(c *gin.Context, message string) {
	if message == "" {
		message = model.MsgBadRequest
	}
	ErrorResponse(c, http.StatusBadRequest, message)
}

// UnauthorizedResponse 未授权响应
func UnauthorizedResponse(c *gin.Context, message string) {
	if message == "" {
		message = model.MsgUnauthorized
	}
	ErrorResponse(c, http.StatusUnauthorized, message)
}

// ForbiddenResponse 权限不足响应
func ForbiddenResponse(c *gin.Context, message string) {
	if message == "" {
		message = model.MsgForbidden
	}
	ErrorResponse(c, http.StatusForbidden, message)
}

// NotFoundResponse 资源不存在响应
func NotFoundResponse(c *gin.Context, message string) {
	if message == "" {
		message = model.MsgNotFound
	}
	ErrorResponse(c, http.StatusNotFound, message)
}

// ServerErrorResponse 服务器错误响应
func ServerErrorResponse(c *gin.Context, message string) {
	if message == "" {
		message = model.MsgServerError
	}
	ErrorResponse(c, http.StatusInternalServerError, message)
}

// PaginationResponse 分页响应
func PaginationResponse(c *gin.Context, total int64, page, pageSize int, data interface{}) {
	response := model.NewPaginationResponse(total, page, pageSize, data)
	c.JSON(http.StatusOK, model.NewSuccessResponse(response))
}

// BindAndValidate 绑定并验证请求参数
func BindAndValidate(c *gin.Context, obj interface{}) error {
	if err := c.ShouldBindJSON(obj); err != nil {
		BadRequestResponse(c, "请求参数格式错误")
		return err
	}
	return nil
}

// BindQueryAndValidate 绑定并验证查询参数
func BindQueryAndValidate(c *gin.Context, obj interface{}) error {
	if err := c.ShouldBindQuery(obj); err != nil {
		BadRequestResponse(c, "查询参数格式错误")
		return err
	}
	return nil
}

// TooManyRequestsResponse 请求过于频繁响应
func TooManyRequestsResponse(c *gin.Context, message string) {
	if message == "" {
		message = "请求过于频繁，请稍后再试"
	}
	ErrorResponse(c, http.StatusTooManyRequests, message)
}
