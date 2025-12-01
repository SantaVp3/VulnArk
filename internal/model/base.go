package model

import (
	"time"
	"gorm.io/gorm"
)

// BaseModel 基础模型，包含公共字段
type BaseModel struct {
	ID        uint           `json:"id" gorm:"primarykey"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `json:"-" gorm:"index"`
}

// PaginationRequest 分页请求参数
type PaginationRequest struct {
	Page     int `json:"page" form:"page" binding:"omitempty,min=1,max=1000"`
	PageSize int `json:"page_size" form:"page_size" binding:"omitempty,min=1,max=50"`
}

// PaginationResponse 分页响应
type PaginationResponse struct {
	Total       int64       `json:"total"`
	Page        int         `json:"page"`
	PageSize    int         `json:"page_size"`
	TotalPages  int         `json:"total_pages"`
	Data        interface{} `json:"data"`
}

// SearchRequest 搜索请求参数
type SearchRequest struct {
	Keyword  string `json:"keyword" form:"keyword" binding:"max=100"`
	Category string `json:"category" form:"category" binding:"max=50"`
	Status   string `json:"status" form:"status" binding:"max=20"`
	PaginationRequest
}

// Response 统一响应结构
type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}

// 响应状态码常量
const (
	CodeSuccess      = 200
	CodeBadRequest   = 400
	CodeUnauthorized = 401
	CodeForbidden    = 403
	CodeNotFound     = 404
	CodeServerError  = 500
)

// 响应消息常量
const (
	MsgSuccess      = "操作成功"
	MsgBadRequest   = "请求参数错误"
	MsgUnauthorized = "未授权访问"
	MsgForbidden    = "权限不足"
	MsgNotFound     = "资源不存在"
	MsgServerError  = "服务器内部错误"
)

// NewSuccessResponse 创建成功响应
func NewSuccessResponse(data interface{}) *Response {
	return &Response{
		Code:    CodeSuccess,
		Message: MsgSuccess,
		Data:    data,
	}
}

// NewErrorResponse 创建错误响应
func NewErrorResponse(code int, message string) *Response {
	return &Response{
		Code:    code,
		Message: message,
	}
}

// NewPaginationResponse 创建分页响应
func NewPaginationResponse(total int64, page, pageSize int, data interface{}) *PaginationResponse {
	totalPages := int((total + int64(pageSize) - 1) / int64(pageSize))
	return &PaginationResponse{
		Total:      total,
		Page:       page,
		PageSize:   pageSize,
		TotalPages: totalPages,
		Data:       data,
	}
}
