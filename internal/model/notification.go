package model

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"time"
)

// Notification 通知模型
type Notification struct {
	ID          uint                   `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time              `json:"created_at"`
	UpdatedAt   time.Time              `json:"updated_at"`
	Title       string                 `json:"title" gorm:"size:255;not null"`
	Content     string                 `json:"content" gorm:"type:text;not null"`
	Type        string                 `json:"type" gorm:"size:50;not null"`
	Level       string                 `json:"level" gorm:"size:20;default:info"`
	RecipientID uint                   `json:"recipient_id" gorm:"not null"`
	SenderID    *uint                  `json:"sender_id"`
	IsRead      bool                   `json:"is_read" gorm:"default:false"`
	ReadAt      *time.Time             `json:"read_at"`
	Data        NotificationData       `json:"data" gorm:"type:json"`
	EntityType  string                 `json:"entity_type" gorm:"size:50"`
	EntityID    *uint                  `json:"entity_id"`
	ExpiresAt   *time.Time             `json:"expires_at"`

	// 关联
	Recipient User  `json:"recipient" gorm:"foreignKey:RecipientID"`
	Sender    *User `json:"sender,omitempty" gorm:"foreignKey:SenderID"`
}

// TableName 指定表名
func (Notification) TableName() string {
	return "notifications"
}

// NotificationData 通知数据
type NotificationData map[string]interface{}

// Value 实现 driver.Valuer 接口
func (d NotificationData) Value() (driver.Value, error) {
	if len(d) == 0 {
		return "{}", nil
	}
	return json.Marshal(d)
}

// Scan 实现 sql.Scanner 接口
func (d *NotificationData) Scan(value interface{}) error {
	if value == nil {
		*d = NotificationData{}
		return nil
	}
	
	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("无法将值转换为[]byte")
	}
	
	return json.Unmarshal(bytes, d)
}

// EmailTemplate 邮件模板模型
type EmailTemplate struct {
	ID          uint      `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
	Name        string    `json:"name" gorm:"size:100;not null;uniqueIndex"`
	Subject     string    `json:"subject" gorm:"size:255;not null"`
	Content     string    `json:"content" gorm:"type:longtext;not null"`
	Type        string    `json:"type" gorm:"size:50;not null"`
	IsActive    bool      `json:"is_active" gorm:"default:true"`
	Variables   string    `json:"variables" gorm:"type:text"`
	CreatorID   uint      `json:"creator_id" gorm:"not null"`

	// 关联
	Creator User `json:"creator" gorm:"foreignKey:CreatorID"`
}

// TableName 指定表名
func (EmailTemplate) TableName() string {
	return "email_templates"
}

// EmailLog 邮件发送日志模型
type EmailLog struct {
	ID          uint      `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time `json:"created_at"`
	To          string    `json:"to" gorm:"size:255;not null"`
	Cc          string    `json:"cc" gorm:"size:500"`
	Bcc         string    `json:"bcc" gorm:"size:500"`
	Subject     string    `json:"subject" gorm:"size:255;not null"`
	Content     string    `json:"content" gorm:"type:longtext"`
	TemplateID  *uint     `json:"template_id"`
	Status      string    `json:"status" gorm:"size:20;default:pending"`
	Error       string    `json:"error" gorm:"type:text"`
	SentAt      *time.Time `json:"sent_at"`
	SenderID    *uint     `json:"sender_id"`

	// 关联
	Template *EmailTemplate `json:"template,omitempty" gorm:"foreignKey:TemplateID"`
	Sender   *User          `json:"sender,omitempty" gorm:"foreignKey:SenderID"`
}

// TableName 指定表名
func (EmailLog) TableName() string {
	return "email_logs"
}

// NotificationSetting 通知设置模型
type NotificationSetting struct {
	ID                    uint `json:"id" gorm:"primarykey"`
	UserID                uint `json:"user_id" gorm:"not null;uniqueIndex"`
	EmailEnabled          bool `json:"email_enabled" gorm:"default:true"`
	SystemEnabled         bool `json:"system_enabled" gorm:"default:true"`
	VulnerabilityAssigned bool `json:"vulnerability_assigned" gorm:"default:true"`
	VulnerabilityUpdated  bool `json:"vulnerability_updated" gorm:"default:true"`
	ReportSubmitted       bool `json:"report_submitted" gorm:"default:true"`
	ReportReviewed        bool `json:"report_reviewed" gorm:"default:true"`
	SystemMaintenance     bool `json:"system_maintenance" gorm:"default:true"`
	SecurityAlert         bool `json:"security_alert" gorm:"default:true"`

	// 关联
	User User `json:"user" gorm:"foreignKey:UserID"`
}

// TableName 指定表名
func (NotificationSetting) TableName() string {
	return "notification_settings"
}

// NotificationCreateRequest 通知创建请求
type NotificationCreateRequest struct {
	Title       string           `json:"title" binding:"required,min=1,max=255"`
	Content     string           `json:"content" binding:"required,min=1"`
	Type        string           `json:"type" binding:"required,oneof=system vulnerability report security maintenance"`
	Level       string           `json:"level" binding:"required,oneof=info warning error success"`
	RecipientID uint             `json:"recipient_id" binding:"required"`
	Data        NotificationData `json:"data"`
	EntityType  string           `json:"entity_type"`
	EntityID    *uint            `json:"entity_id"`
	ExpiresAt   *time.Time       `json:"expires_at"`
}

// NotificationSearchRequest 通知搜索请求
type NotificationSearchRequest struct {
	Keyword     string `json:"keyword" form:"keyword"`
	Type        string `json:"type" form:"type"`
	Level       string `json:"level" form:"level"`
	IsRead      string `json:"is_read" form:"is_read"`
	RecipientID string `json:"recipient_id" form:"recipient_id"`
	StartDate   string `json:"start_date" form:"start_date"`
	EndDate     string `json:"end_date" form:"end_date"`
	PaginationRequest
}

// EmailTemplateCreateRequest 邮件模板创建请求
type EmailTemplateCreateRequest struct {
	Name      string `json:"name" binding:"required,min=1,max=100"`
	Subject   string `json:"subject" binding:"required,min=1,max=255"`
	Content   string `json:"content" binding:"required,min=1"`
	Type      string `json:"type" binding:"required,oneof=vulnerability report system security"`
	Variables string `json:"variables"`
}

// EmailTemplateUpdateRequest 邮件模板更新请求
type EmailTemplateUpdateRequest struct {
	Name      string `json:"name" binding:"omitempty,min=1,max=100"`
	Subject   string `json:"subject" binding:"omitempty,min=1,max=255"`
	Content   string `json:"content" binding:"omitempty,min=1"`
	Type      string `json:"type" binding:"omitempty,oneof=vulnerability report system security"`
	Variables string `json:"variables"`
	IsActive  *bool  `json:"is_active"`
}

// EmailSendRequest 邮件发送请求
type EmailSendRequest struct {
	To         []string               `json:"to" binding:"required,min=1"`
	Cc         []string               `json:"cc"`
	Bcc        []string               `json:"bcc"`
	Subject    string                 `json:"subject" binding:"required,min=1,max=255"`
	Content    string                 `json:"content" binding:"required,min=1"`
	TemplateID *uint                  `json:"template_id"`
	Variables  map[string]interface{} `json:"variables"`
}

// NotificationSettingUpdateRequest 通知设置更新请求
type NotificationSettingUpdateRequest struct {
	EmailEnabled          *bool `json:"email_enabled"`
	SystemEnabled         *bool `json:"system_enabled"`
	VulnerabilityAssigned *bool `json:"vulnerability_assigned"`
	VulnerabilityUpdated  *bool `json:"vulnerability_updated"`
	ReportSubmitted       *bool `json:"report_submitted"`
	ReportReviewed        *bool `json:"report_reviewed"`
	SystemMaintenance     *bool `json:"system_maintenance"`
	SecurityAlert         *bool `json:"security_alert"`
}

// NotificationStats 通知统计
type NotificationStats struct {
	Total      int64                `json:"total"`
	Unread     int64                `json:"unread"`
	ByType     map[string]int64     `json:"by_type"`
	ByLevel    map[string]int64     `json:"by_level"`
	Recent     []NotificationSummary `json:"recent"`
}

// NotificationSummary 通知摘要
type NotificationSummary struct {
	ID          uint      `json:"id"`
	Title       string    `json:"title"`
	Type        string    `json:"type"`
	Level       string    `json:"level"`
	IsRead      bool      `json:"is_read"`
	CreatedAt   time.Time `json:"created_at"`
	SenderName  string    `json:"sender_name"`
}

// NotificationInfo 通知信息（用于响应）
type NotificationInfo struct {
	ID          uint             `json:"id"`
	CreatedAt   time.Time        `json:"created_at"`
	UpdatedAt   time.Time        `json:"updated_at"`
	Title       string           `json:"title"`
	Content     string           `json:"content"`
	Type        string           `json:"type"`
	TypeText    string           `json:"type_text"`
	Level       string           `json:"level"`
	LevelText   string           `json:"level_text"`
	RecipientID uint             `json:"recipient_id"`
	SenderID    *uint            `json:"sender_id"`
	IsRead      bool             `json:"is_read"`
	ReadAt      *time.Time       `json:"read_at"`
	Data        NotificationData `json:"data"`
	EntityType  string           `json:"entity_type"`
	EntityID    *uint            `json:"entity_id"`
	ExpiresAt   *time.Time       `json:"expires_at"`
	Recipient   UserInfo         `json:"recipient"`
	Sender      *UserInfo        `json:"sender,omitempty"`
}

// ToNotificationInfo 转换为通知信息
func (n *Notification) ToNotificationInfo() NotificationInfo {
	info := NotificationInfo{
		ID:          n.ID,
		CreatedAt:   n.CreatedAt,
		UpdatedAt:   n.UpdatedAt,
		Title:       n.Title,
		Content:     n.Content,
		Type:        n.Type,
		TypeText:    n.GetTypeText(),
		Level:       n.Level,
		LevelText:   n.GetLevelText(),
		RecipientID: n.RecipientID,
		SenderID:    n.SenderID,
		IsRead:      n.IsRead,
		ReadAt:      n.ReadAt,
		Data:        n.Data,
		EntityType:  n.EntityType,
		EntityID:    n.EntityID,
		ExpiresAt:   n.ExpiresAt,
		Recipient:   n.Recipient.ToUserInfo(),
	}
	
	if n.Sender != nil {
		senderInfo := n.Sender.ToUserInfo()
		info.Sender = &senderInfo
	}
	
	return info
}

// GetTypeText 获取通知类型文本
func (n *Notification) GetTypeText() string {
	switch n.Type {
	case "system":
		return "系统通知"
	case "vulnerability":
		return "漏洞通知"
	case "report":
		return "报告通知"
	case "security":
		return "安全通知"
	case "maintenance":
		return "维护通知"
	default:
		return "未知类型"
	}
}

// GetLevelText 获取通知级别文本
func (n *Notification) GetLevelText() string {
	switch n.Level {
	case "info":
		return "信息"
	case "warning":
		return "警告"
	case "error":
		return "错误"
	case "success":
		return "成功"
	default:
		return "未知级别"
	}
}

// 通知类型常量
const (
	NotificationTypeSystem       = "system"
	NotificationTypeVulnerability = "vulnerability"
	NotificationTypeReport       = "report"
	NotificationTypeSecurity     = "security"
	NotificationTypeMaintenance  = "maintenance"
)

// 通知级别常量
const (
	NotificationLevelInfo    = "info"
	NotificationLevelWarning = "warning"
	NotificationLevelError   = "error"
	NotificationLevelSuccess = "success"
)

// 邮件状态常量
const (
	EmailStatusPending = "pending"
	EmailStatusSent    = "sent"
	EmailStatusFailed  = "failed"
)

// 邮件模板类型常量
const (
	EmailTemplateTypeVulnerability = "vulnerability"
	EmailTemplateTypeReport        = "report"
	EmailTemplateTypeSystem        = "system"
	EmailTemplateTypeSecurity      = "security"
)
