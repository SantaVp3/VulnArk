package model

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"time"
)

// Report 渗透测试报告模型
type Report struct {
	ID           uint       `json:"id" gorm:"primarykey"`
	CreatedAt    time.Time  `json:"created_at"`
	UpdatedAt    time.Time  `json:"updated_at"`
	Title        string     `json:"title" gorm:"size:255;not null" binding:"required"`
	Description  string     `json:"description" gorm:"type:text"`
	Type         string     `json:"type" gorm:"size:50;not null" binding:"required"`
	Severity     string     `json:"severity" gorm:"size:20;default:medium"`
	Status       string     `json:"status" gorm:"size:20;default:draft"`
	AssetIDs     AssetIDList `json:"asset_ids" gorm:"type:json"` // 关联的资产ID列表
	Tags         TagList    `json:"tags" gorm:"type:json"`       // 标签
	FilePath     string     `json:"file_path" gorm:"size:500;not null"`
	FileName     string     `json:"file_name" gorm:"size:255;not null"`
	FileSize     int64      `json:"file_size" gorm:"not null"`
	FileType     string     `json:"file_type" gorm:"size:100"`
	DownloadURL  string     `json:"download_url" gorm:"size:500"`
	UploaderID   uint       `json:"uploader_id" gorm:"not null"`
	TestDate     *time.Time `json:"test_date"`     // 测试日期
	SubmittedAt  *time.Time `json:"submitted_at"`  // 提交日期
	ReviewedAt   *time.Time `json:"reviewed_at"`   // 审核日期
	ReviewerID   *uint      `json:"reviewer_id"`   // 审核人ID
	ReviewNotes  string     `json:"review_notes" gorm:"type:text"` // 审核备注

	// 关联
	Uploader User  `json:"uploader" gorm:"foreignKey:UploaderID"`
	Reviewer *User `json:"reviewer,omitempty" gorm:"foreignKey:ReviewerID"`
}

// TableName 指定表名
func (Report) TableName() string {
	return "reports"
}

// AssetIDList 资产ID列表
type AssetIDList []uint

// Value 实现 driver.Valuer 接口
func (a AssetIDList) Value() (driver.Value, error) {
	if len(a) == 0 {
		return "[]", nil
	}
	return json.Marshal(a)
}

// Scan 实现 sql.Scanner 接口
func (a *AssetIDList) Scan(value interface{}) error {
	if value == nil {
		*a = AssetIDList{}
		return nil
	}

	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("无法将值转换为[]byte")
	}

	return json.Unmarshal(bytes, a)
}

// TagList 标签列表
type TagList []string

// Value 实现 driver.Valuer 接口
func (t TagList) Value() (driver.Value, error) {
	if len(t) == 0 {
		return "[]", nil
	}
	return json.Marshal(t)
}

// Scan 实现 sql.Scanner 接口
func (t *TagList) Scan(value interface{}) error {
	if value == nil {
		*t = TagList{}
		return nil
	}

	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("无法将值转换为[]byte")
	}

	return json.Unmarshal(bytes, t)
}

// ReportComment 报告评论模型
type ReportComment struct {
	ID        uint      `json:"id" gorm:"primarykey"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	ReportID  uint      `json:"report_id" gorm:"not null"`
	Content   string    `json:"content" gorm:"type:text;not null"`
	UserID    uint      `json:"user_id" gorm:"not null"`

	// 关联
	User User `json:"user" gorm:"foreignKey:UserID"`
}

// TableName 指定表名
func (ReportComment) TableName() string {
	return "report_comments"
}

// FileAttachment 文件附件模型
type FileAttachment struct {
	ID           uint      `json:"id" gorm:"primarykey"`
	CreatedAt    time.Time `json:"created_at"`
	Filename     string    `json:"filename" gorm:"size:255;not null"`
	OriginalName string    `json:"original_name" gorm:"size:255;not null"`
	FilePath     string    `json:"file_path" gorm:"size:500;not null"`
	FileSize     int64     `json:"file_size" gorm:"not null"`
	FileType     string    `json:"file_type" gorm:"size:100"`
	MimeType     string    `json:"mime_type" gorm:"size:100"`
	DownloadURL  string    `json:"download_url" gorm:"size:500"`
	UploaderID   uint      `json:"uploader_id" gorm:"not null"`
	EntityType   string    `json:"entity_type" gorm:"size:50"` // vulnerability, report, etc.
	EntityID     uint      `json:"entity_id"`
	ExpiresAt    *time.Time `json:"expires_at"`

	// 关联
	Uploader User `json:"uploader" gorm:"foreignKey:UploaderID"`
}

// TableName 指定表名
func (FileAttachment) TableName() string {
	return "file_attachments"
}

// ReportUploadRequest 报告上传请求
type ReportUploadRequest struct {
	Title       string      `json:"title" binding:"required,min=1,max=255"`
	Description string      `json:"description"`
	Type        string      `json:"type" binding:"required,oneof=penetration_test vulnerability_assessment security_audit compliance_check other"`
	Severity    string      `json:"severity" binding:"required,oneof=critical high medium low info"`
	AssetIDs    AssetIDList `json:"asset_ids"`
	Tags        TagList     `json:"tags"`
	TestDate    *time.Time  `json:"test_date"`
}

// ReportUpdateRequest 报告更新请求
type ReportUpdateRequest struct {
	Title       string      `json:"title" binding:"omitempty,min=1,max=255"`
	Description string      `json:"description"`
	Type        string      `json:"type" binding:"omitempty,oneof=penetration_test vulnerability_assessment security_audit compliance_check other"`
	Severity    string      `json:"severity" binding:"omitempty,oneof=critical high medium low info"`
	AssetIDs    AssetIDList `json:"asset_ids"`
	Tags        TagList     `json:"tags"`
	TestDate    *time.Time  `json:"test_date"`
}

// ReportSearchRequest 报告搜索请求
type ReportSearchRequest struct {
	Keyword    string `json:"keyword" form:"keyword"`
	Type       string `json:"type" form:"type"`
	Severity   string `json:"severity" form:"severity"`
	Status     string `json:"status" form:"status"`
	UploaderID string `json:"uploader_id" form:"uploader_id"`
	AssetID    string `json:"asset_id" form:"asset_id"`
	Tag        string `json:"tag" form:"tag"`
	StartDate  string `json:"start_date" form:"start_date"`
	EndDate    string `json:"end_date" form:"end_date"`
	PaginationRequest
}

// ReportReviewRequest 报告审核请求
type ReportReviewRequest struct {
	Status      string `json:"status" binding:"required,oneof=approved rejected"`
	ReviewNotes string `json:"review_notes"`
}

// ReportCommentRequest 报告评论请求
type ReportCommentRequest struct {
	Content string `json:"content" binding:"required,min=1"`
}

// ReportStats 报告统计
type ReportStats struct {
	Total         int64                `json:"total"`
	ByType        map[string]int64     `json:"by_type"`
	BySeverity    map[string]int64     `json:"by_severity"`
	ByStatus      map[string]int64     `json:"by_status"`
	RecentReports []ReportSummary      `json:"recent_reports"`
	TotalSize     int64                `json:"total_size"`
}

// ReportSummary 报告摘要
type ReportSummary struct {
	ID           uint      `json:"id"`
	Title        string    `json:"title"`
	Type         string    `json:"type"`
	Severity     string    `json:"severity"`
	Status       string    `json:"status"`
	FileSize     int64     `json:"file_size"`
	CreatedAt    time.Time `json:"created_at"`
	UploaderName string    `json:"uploader_name"`
}

// ReportInfo 报告信息（用于响应）
type ReportInfo struct {
	ID           uint        `json:"id"`
	CreatedAt    time.Time   `json:"created_at"`
	UpdatedAt    time.Time   `json:"updated_at"`
	Title        string      `json:"title"`
	Description  string      `json:"description"`
	Type         string      `json:"type"`
	TypeText     string      `json:"type_text"`
	Severity     string      `json:"severity"`
	SeverityText string      `json:"severity_text"`
	Status       string      `json:"status"`
	StatusText   string      `json:"status_text"`
	AssetIDs     AssetIDList `json:"asset_ids"`
	Tags         TagList     `json:"tags"`
	FilePath     string      `json:"file_path"`
	FileName     string      `json:"file_name"`
	FileSize     int64       `json:"file_size"`
	FileType     string      `json:"file_type"`
	DownloadURL  string      `json:"download_url"`
	UploaderID   uint        `json:"uploader_id"`
	TestDate     *time.Time  `json:"test_date"`
	SubmittedAt  *time.Time  `json:"submitted_at"`
	ReviewedAt   *time.Time  `json:"reviewed_at"`
	ReviewerID   *uint       `json:"reviewer_id"`
	ReviewNotes  string      `json:"review_notes"`
	Uploader     UserInfo    `json:"uploader"`
	Reviewer     *UserInfo   `json:"reviewer,omitempty"`
}

// ToReportInfo 转换为报告信息
func (r *Report) ToReportInfo() ReportInfo {
	info := ReportInfo{
		ID:           r.ID,
		CreatedAt:    r.CreatedAt,
		UpdatedAt:    r.UpdatedAt,
		Title:        r.Title,
		Description:  r.Description,
		Type:         r.Type,
		TypeText:     r.GetTypeText(),
		Severity:     r.Severity,
		SeverityText: r.GetSeverityText(),
		Status:       r.Status,
		StatusText:   r.GetStatusText(),
		AssetIDs:     r.AssetIDs,
		Tags:         r.Tags,
		FilePath:     r.FilePath,
		FileName:     r.FileName,
		FileSize:     r.FileSize,
		FileType:     r.FileType,
		DownloadURL:  r.DownloadURL,
		UploaderID:   r.UploaderID,
		TestDate:     r.TestDate,
		SubmittedAt:  r.SubmittedAt,
		ReviewedAt:   r.ReviewedAt,
		ReviewerID:   r.ReviewerID,
		ReviewNotes:  r.ReviewNotes,
		Uploader:     r.Uploader.ToUserInfo(),
	}

	if r.Reviewer != nil {
		reviewerInfo := r.Reviewer.ToUserInfo()
		info.Reviewer = &reviewerInfo
	}

	return info
}

// GetTypeText 获取报告类型文本
func (r *Report) GetTypeText() string {
	switch r.Type {
	case "penetration_test":
		return "渗透测试报告"
	case "vulnerability_assessment":
		return "漏洞评估报告"
	case "security_audit":
		return "安全审计报告"
	case "compliance_check":
		return "合规检查报告"
	case "other":
		return "其他报告"
	default:
		return "未知类型"
	}
}

// GetSeverityText 获取严重程度文本
func (r *Report) GetSeverityText() string {
	switch r.Severity {
	case "critical":
		return "严重"
	case "high":
		return "高危"
	case "medium":
		return "中危"
	case "low":
		return "低危"
	case "info":
		return "信息"
	default:
		return "未知"
	}
}

// GetStatusText 获取报告状态文本
func (r *Report) GetStatusText() string {
	switch r.Status {
	case "draft":
		return "草稿"
	case "submitted":
		return "已提交"
	case "reviewing":
		return "审核中"
	case "approved":
		return "已通过"
	case "rejected":
		return "已拒绝"
	case "archived":
		return "已归档"
	default:
		return "未知状态"
	}
}

// 报告类型常量
const (
	ReportTypePenetrationTest       = "penetration_test"
	ReportTypeVulnerabilityAssessment = "vulnerability_assessment"
	ReportTypeSecurityAudit         = "security_audit"
	ReportTypeComplianceCheck       = "compliance_check"
	ReportTypeOther                 = "other"
)

// 报告严重程度常量
const (
	ReportSeverityCritical = "critical"
	ReportSeverityHigh     = "high"
	ReportSeverityMedium   = "medium"
	ReportSeverityLow      = "low"
	ReportSeverityInfo     = "info"
)

// 报告状态常量
const (
	ReportStatusDraft     = "draft"
	ReportStatusSubmitted = "submitted"
	ReportStatusReviewing = "reviewing"
	ReportStatusApproved  = "approved"
	ReportStatusRejected  = "rejected"
	ReportStatusArchived  = "archived"
)

// 文件实体类型常量
const (
	EntityTypeVulnerability = "vulnerability"
	EntityTypeReport        = "report"
	EntityTypeAsset         = "asset"
)
