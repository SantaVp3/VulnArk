package model

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"time"
)

// VulnerabilityAssignment 漏洞分配记录模型
type VulnerabilityAssignment struct {
	ID                uint      `json:"id" gorm:"primarykey"`
	CreatedAt         time.Time `json:"created_at"`
	VulnerabilityID   uint      `json:"vulnerability_id" gorm:"not null"`
	AssigneeID        uint      `json:"assignee_id" gorm:"not null"`
	AssignerID        uint      `json:"assigner_id" gorm:"not null"`
	AssignmentType    string    `json:"assignment_type" gorm:"size:20;default:manual"` // manual, auto
	AssignmentReason  string    `json:"assignment_reason" gorm:"type:text"`
	AssignedAt        time.Time `json:"assigned_at" gorm:"default:CURRENT_TIMESTAMP"`

	// 关联
	Vulnerability Vulnerability `json:"vulnerability" gorm:"foreignKey:VulnerabilityID"`
	Assignee      User          `json:"assignee" gorm:"foreignKey:AssigneeID"`
	Assigner      User          `json:"assigner" gorm:"foreignKey:AssignerID"`
}

// TableName 指定表名
func (VulnerabilityAssignment) TableName() string {
	return "vulnerability_assignments"
}

// AssignmentRule 分配规则模型
type AssignmentRule struct {
	ID         uint                   `json:"id" gorm:"primarykey"`
	CreatedAt  time.Time              `json:"created_at"`
	UpdatedAt  time.Time              `json:"updated_at"`
	Name       string                 `json:"name" gorm:"size:100;not null"`
	RuleType   string                 `json:"rule_type" gorm:"size:50;not null"` // asset_type, severity, department, owasp_category
	Conditions AssignmentConditions   `json:"conditions" gorm:"type:json;not null"`
	AssigneeID uint                   `json:"assignee_id" gorm:"not null"`
	Priority   int                    `json:"priority" gorm:"default:0"`
	IsActive   bool                   `json:"is_active" gorm:"default:true"`

	// 关联
	Assignee User `json:"assignee" gorm:"foreignKey:AssigneeID"`
}

// TableName 指定表名
func (AssignmentRule) TableName() string {
	return "assignment_rules"
}

// AssignmentConditions 分配条件
type AssignmentConditions map[string]interface{}

// Value 实现 driver.Valuer 接口
func (c AssignmentConditions) Value() (driver.Value, error) {
	if len(c) == 0 {
		return "{}", nil
	}
	return json.Marshal(c)
}

// Scan 实现 sql.Scanner 接口
func (c *AssignmentConditions) Scan(value interface{}) error {
	if value == nil {
		*c = AssignmentConditions{}
		return nil
	}
	
	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("无法将值转换为[]byte")
	}
	
	return json.Unmarshal(bytes, c)
}

// VulnerabilityTimeline 漏洞时间线模型
type VulnerabilityTimeline struct {
	ID              uint      `json:"id" gorm:"primarykey"`
	CreatedAt       time.Time `json:"created_at"`
	VulnerabilityID uint      `json:"vulnerability_id" gorm:"not null"`
	Action          string    `json:"action" gorm:"size:50;not null"` // created, assigned, status_changed, updated, commented
	Description     string    `json:"description" gorm:"type:text"`
	OldValue        string    `json:"old_value" gorm:"type:text"`
	NewValue        string    `json:"new_value" gorm:"type:text"`
	UserID          uint      `json:"user_id" gorm:"not null"`

	// 关联
	User User `json:"user" gorm:"foreignKey:UserID"`
}

// TableName 指定表名
func (VulnerabilityTimeline) TableName() string {
	return "vulnerability_timeline"
}

// AssignmentRuleCreateRequest 分配规则创建请求
type AssignmentRuleCreateRequest struct {
	Name       string               `json:"name" binding:"required,min=1,max=100"`
	RuleType   string               `json:"rule_type" binding:"required,oneof=asset_type severity department owasp_category"`
	Conditions AssignmentConditions `json:"conditions" binding:"required"`
	AssigneeID uint                 `json:"assignee_id" binding:"required"`
	Priority   int                  `json:"priority"`
}

// AssignmentRuleUpdateRequest 分配规则更新请求
type AssignmentRuleUpdateRequest struct {
	Name       string               `json:"name" binding:"omitempty,min=1,max=100"`
	RuleType   string               `json:"rule_type" binding:"omitempty,oneof=asset_type severity department owasp_category"`
	Conditions AssignmentConditions `json:"conditions"`
	AssigneeID *uint                `json:"assignee_id"`
	Priority   *int                 `json:"priority"`
	IsActive   *bool                `json:"is_active"`
}

// AssignmentRuleSearchRequest 分配规则搜索请求
type AssignmentRuleSearchRequest struct {
	Keyword    string `json:"keyword" form:"keyword"`
	RuleType   string `json:"rule_type" form:"rule_type"`
	AssigneeID string `json:"assignee_id" form:"assignee_id"`
	IsActive   string `json:"is_active" form:"is_active"`
	PaginationRequest
}

// VulnerabilityAssignRequest 漏洞分配请求
type VulnerabilityAssignRequest struct {
	AssigneeID       uint   `json:"assignee_id" binding:"required"`
	AssignmentReason string `json:"assignment_reason"`
}

// BatchAssignRequest 批量分配请求
type BatchAssignRequest struct {
	VulnerabilityIDs []uint `json:"vulnerability_ids" binding:"required,min=1"`
	AssigneeID       uint   `json:"assignee_id" binding:"required"`
	AssignmentReason string `json:"assignment_reason"`
}

// AutoAssignRequest 自动分配请求
type AutoAssignRequest struct {
	VulnerabilityIDs []uint `json:"vulnerability_ids"`
	ForceReassign    bool   `json:"force_reassign"` // 是否强制重新分配已分配的漏洞
}

// TimelineEntry 时间线条目
type TimelineEntry struct {
	ID          uint      `json:"id"`
	CreatedAt   time.Time `json:"created_at"`
	Action      string    `json:"action"`
	Description string    `json:"description"`
	OldValue    string    `json:"old_value"`
	NewValue    string    `json:"new_value"`
	User        UserInfo  `json:"user"`
}

// AssignmentStats 分配统计
type AssignmentStats struct {
	TotalAssignments    int64                    `json:"total_assignments"`
	AutoAssignments     int64                    `json:"auto_assignments"`
	ManualAssignments   int64                    `json:"manual_assignments"`
	ByAssignee          map[string]int64         `json:"by_assignee"`
	ByRuleType          map[string]int64         `json:"by_rule_type"`
	RecentAssignments   []AssignmentHistoryItem  `json:"recent_assignments"`
}

// AssignmentHistoryItem 分配历史项
type AssignmentHistoryItem struct {
	ID               uint      `json:"id"`
	VulnerabilityID  uint      `json:"vulnerability_id"`
	VulnerabilityTitle string  `json:"vulnerability_title"`
	AssigneeID       uint      `json:"assignee_id"`
	AssigneeName     string    `json:"assignee_name"`
	AssignerID       uint      `json:"assigner_id"`
	AssignerName     string    `json:"assigner_name"`
	AssignmentType   string    `json:"assignment_type"`
	AssignedAt       time.Time `json:"assigned_at"`
}

// 分配类型常量
const (
	AssignmentTypeManual = "manual"
	AssignmentTypeAuto   = "auto"
)

// 规则类型常量
const (
	RuleTypeAssetType     = "asset_type"
	RuleTypeSeverity      = "severity"
	RuleTypeDepartment    = "department"
	RuleTypeOWASPCategory = "owasp_category"
)

// 时间线动作常量
const (
	TimelineActionCreated       = "created"
	TimelineActionAssigned      = "assigned"
	TimelineActionStatusChanged = "status_changed"
	TimelineActionUpdated       = "updated"
	TimelineActionCommented     = "commented"
	TimelineActionReassigned    = "reassigned"
)
