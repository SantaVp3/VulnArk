package model

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"time"
)

// Permission 权限模型
type Permission struct {
	ID          uint      `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
	Name        string    `json:"name" gorm:"size:100;not null;uniqueIndex"`
	DisplayName string    `json:"display_name" gorm:"size:100;not null"`
	Description string    `json:"description" gorm:"type:text"`
	Module      string    `json:"module" gorm:"size:50;not null"`
	Action      string    `json:"action" gorm:"size:50;not null"`
	Resource    string    `json:"resource" gorm:"size:50;not null"`
	IsActive    bool      `json:"is_active" gorm:"default:true"`
}

// TableName 指定表名
func (Permission) TableName() string {
	return "permissions"
}

// RolePermission 角色权限关联模型
type RolePermission struct {
	ID           uint `json:"id" gorm:"primarykey"`
	RoleID       uint `json:"role_id" gorm:"not null"`
	PermissionID uint `json:"permission_id" gorm:"not null"`

	// 关联
	Role       Role       `json:"role" gorm:"foreignKey:RoleID"`
	Permission Permission `json:"permission" gorm:"foreignKey:PermissionID"`
}

// TableName 指定表名
func (RolePermission) TableName() string {
	return "role_permissions"
}

// SystemConfig 系统配置模型
type SystemConfig struct {
	ID          uint        `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time   `json:"created_at"`
	UpdatedAt   time.Time   `json:"updated_at"`
	Key         string      `json:"key" gorm:"size:100;not null;uniqueIndex"`
	Value       ConfigValue `json:"value" gorm:"type:json;not null"`
	Type        string      `json:"type" gorm:"size:20;not null"`
	Category    string      `json:"category" gorm:"size:50;not null"`
	DisplayName string      `json:"display_name" gorm:"size:100;not null"`
	Description string      `json:"description" gorm:"type:text"`
	IsPublic    bool        `json:"is_public" gorm:"default:false"`
	IsEditable  bool        `json:"is_editable" gorm:"default:true"`
	Validation  string      `json:"validation" gorm:"type:text"`
	DefaultValue ConfigValue `json:"default_value" gorm:"type:json"`
}

// TableName 指定表名
func (SystemConfig) TableName() string {
	return "system_configs"
}

// ConfigValue 配置值
type ConfigValue map[string]interface{}

// Value 实现 driver.Valuer 接口
func (c ConfigValue) Value() (driver.Value, error) {
	if len(c) == 0 {
		return "{}", nil
	}
	return json.Marshal(c)
}

// Scan 实现 sql.Scanner 接口
func (c *ConfigValue) Scan(value interface{}) error {
	if value == nil {
		*c = ConfigValue{}
		return nil
	}
	
	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("无法将值转换为[]byte")
	}
	
	return json.Unmarshal(bytes, c)
}

// GetString 获取字符串值
func (c ConfigValue) GetString(key string) string {
	if val, ok := c[key]; ok {
		if str, ok := val.(string); ok {
			return str
		}
	}
	return ""
}

// GetInt 获取整数值
func (c ConfigValue) GetInt(key string) int {
	if val, ok := c[key]; ok {
		if num, ok := val.(float64); ok {
			return int(num)
		}
	}
	return 0
}

// GetBool 获取布尔值
func (c ConfigValue) GetBool(key string) bool {
	if val, ok := c[key]; ok {
		if b, ok := val.(bool); ok {
			return b
		}
	}
	return false
}

// AuditLog 审计日志模型
type AuditLog struct {
	ID         uint      `json:"id" gorm:"primarykey"`
	CreatedAt  time.Time `json:"created_at"`
	UserID     *uint     `json:"user_id"`
	Username   string    `json:"username" gorm:"size:50"`
	Action     string    `json:"action" gorm:"size:100;not null"`
	Resource   string    `json:"resource" gorm:"size:100;not null"`
	ResourceID *uint     `json:"resource_id"`
	Method     string    `json:"method" gorm:"size:10"`
	Path       string    `json:"path" gorm:"size:255"`
	IP         string    `json:"ip" gorm:"size:45"`
	UserAgent  string    `json:"user_agent" gorm:"size:500"`
	Request    string    `json:"request" gorm:"type:text"`
	Response   string    `json:"response" gorm:"type:text"`
	Status     int       `json:"status"`
	Duration   int64     `json:"duration"` // 毫秒
	Error      string    `json:"error" gorm:"type:text"`

	// 关联
	User *User `json:"user,omitempty" gorm:"foreignKey:UserID"`
}

// TableName 指定表名
func (AuditLog) TableName() string {
	return "audit_logs"
}

// PermissionCreateRequest 权限创建请求
type PermissionCreateRequest struct {
	Name        string `json:"name" binding:"required,min=1,max=100"`
	DisplayName string `json:"display_name" binding:"required,min=1,max=100"`
	Description string `json:"description"`
	Module      string `json:"module" binding:"required,min=1,max=50"`
	Action      string `json:"action" binding:"required,min=1,max=50"`
	Resource    string `json:"resource" binding:"required,min=1,max=50"`
}

// PermissionUpdateRequest 权限更新请求
type PermissionUpdateRequest struct {
	DisplayName string `json:"display_name" binding:"omitempty,min=1,max=100"`
	Description string `json:"description"`
	IsActive    *bool  `json:"is_active"`
}

// RolePermissionRequest 角色权限分配请求
type RolePermissionRequest struct {
	PermissionIDs []uint `json:"permission_ids" binding:"required"`
}

// SystemConfigUpdateRequest 系统配置更新请求
type SystemConfigUpdateRequest struct {
	Value ConfigValue `json:"value" binding:"required"`
}

// AuditLogSearchRequest 审计日志搜索请求
type AuditLogSearchRequest struct {
	Keyword    string `json:"keyword" form:"keyword"`
	UserID     string `json:"user_id" form:"user_id"`
	Username   string `json:"username" form:"username"`
	Action     string `json:"action" form:"action"`
	Resource   string `json:"resource" form:"resource"`
	Method     string `json:"method" form:"method"`
	Status     string `json:"status" form:"status"`
	IP         string `json:"ip" form:"ip"`
	StartDate  string `json:"start_date" form:"start_date"`
	EndDate    string `json:"end_date" form:"end_date"`
	PaginationRequest
}

// PermissionInfo 权限信息（用于响应）
type PermissionInfo struct {
	ID          uint   `json:"id"`
	Name        string `json:"name"`
	DisplayName string `json:"display_name"`
	Description string `json:"description"`
	Module      string `json:"module"`
	Action      string `json:"action"`
	Resource    string `json:"resource"`
	IsActive    bool   `json:"is_active"`
	CreatedAt   time.Time `json:"created_at"`
}

// ToPermissionInfo 转换为权限信息
func (p *Permission) ToPermissionInfo() PermissionInfo {
	return PermissionInfo{
		ID:          p.ID,
		Name:        p.Name,
		DisplayName: p.DisplayName,
		Description: p.Description,
		Module:      p.Module,
		Action:      p.Action,
		Resource:    p.Resource,
		IsActive:    p.IsActive,
		CreatedAt:   p.CreatedAt,
	}
}

// SystemConfigInfo 系统配置信息（用于响应）
type SystemConfigInfo struct {
	ID           uint        `json:"id"`
	Key          string      `json:"key"`
	Value        ConfigValue `json:"value"`
	Type         string      `json:"type"`
	Category     string      `json:"category"`
	DisplayName  string      `json:"display_name"`
	Description  string      `json:"description"`
	IsPublic     bool        `json:"is_public"`
	IsEditable   bool        `json:"is_editable"`
	Validation   string      `json:"validation"`
	DefaultValue ConfigValue `json:"default_value"`
	UpdatedAt    time.Time   `json:"updated_at"`
}

// ToSystemConfigInfo 转换为系统配置信息
func (c *SystemConfig) ToSystemConfigInfo() SystemConfigInfo {
	return SystemConfigInfo{
		ID:           c.ID,
		Key:          c.Key,
		Value:        c.Value,
		Type:         c.Type,
		Category:     c.Category,
		DisplayName:  c.DisplayName,
		Description:  c.Description,
		IsPublic:     c.IsPublic,
		IsEditable:   c.IsEditable,
		Validation:   c.Validation,
		DefaultValue: c.DefaultValue,
		UpdatedAt:    c.UpdatedAt,
	}
}

// AuditLogInfo 审计日志信息（用于响应）
type AuditLogInfo struct {
	ID         uint      `json:"id"`
	CreatedAt  time.Time `json:"created_at"`
	UserID     *uint     `json:"user_id"`
	Username   string    `json:"username"`
	Action     string    `json:"action"`
	Resource   string    `json:"resource"`
	ResourceID *uint     `json:"resource_id"`
	Method     string    `json:"method"`
	Path       string    `json:"path"`
	IP         string    `json:"ip"`
	UserAgent  string    `json:"user_agent"`
	Status     int       `json:"status"`
	Duration   int64     `json:"duration"`
	Error      string    `json:"error"`
	User       *UserInfo `json:"user,omitempty"`
}

// ToAuditLogInfo 转换为审计日志信息
func (a *AuditLog) ToAuditLogInfo() AuditLogInfo {
	info := AuditLogInfo{
		ID:         a.ID,
		CreatedAt:  a.CreatedAt,
		UserID:     a.UserID,
		Username:   a.Username,
		Action:     a.Action,
		Resource:   a.Resource,
		ResourceID: a.ResourceID,
		Method:     a.Method,
		Path:       a.Path,
		IP:         a.IP,
		UserAgent:  a.UserAgent,
		Status:     a.Status,
		Duration:   a.Duration,
		Error:      a.Error,
	}
	
	if a.User != nil {
		userInfo := a.User.ToUserInfo()
		info.User = &userInfo
	}
	
	return info
}

// 权限模块常量
const (
	PermissionModuleUser           = "user"
	PermissionModuleRole           = "role"
	PermissionModuleAsset          = "asset"
	PermissionModuleVulnerability  = "vulnerability"
	PermissionModuleReport         = "report"
	PermissionModuleNotification   = "notification"
	PermissionModuleSystem         = "system"
	PermissionModuleAudit          = "audit"
)

// 权限动作常量
const (
	PermissionActionCreate = "create"
	PermissionActionRead   = "read"
	PermissionActionUpdate = "update"
	PermissionActionDelete = "delete"
	PermissionActionManage = "manage"
	PermissionActionAssign = "assign"
	PermissionActionReview = "review"
	PermissionActionExport = "export"
	PermissionActionImport = "import"
)

// 配置类型常量
const (
	ConfigTypeString  = "string"
	ConfigTypeInt     = "int"
	ConfigTypeBool    = "bool"
	ConfigTypeFloat   = "float"
	ConfigTypeJSON    = "json"
	ConfigTypeArray   = "array"
	ConfigTypeObject  = "object"
)

// 配置分类常量
const (
	ConfigCategorySystem       = "system"
	ConfigCategorySecurity     = "security"
	ConfigCategoryEmail        = "email"
	ConfigCategoryStorage      = "storage"
	ConfigCategoryNotification = "notification"
	ConfigCategoryAudit        = "audit"
)
