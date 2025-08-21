package model

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"time"
)

// KnowledgeBase 知识库模型
type KnowledgeBase struct {
	ID          uint      `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
	Title       string    `json:"title" gorm:"size:255;not null"`
	Content     string    `json:"content" gorm:"type:longtext;not null"`
	Summary     string    `json:"summary" gorm:"type:text"`
	Category    string    `json:"category" gorm:"size:50;not null"`
	Type        string    `json:"type" gorm:"size:50;not null"`
	Severity    string    `json:"severity" gorm:"size:20"`
	Status      string    `json:"status" gorm:"size:20;default:published"`
	ViewCount   int       `json:"view_count" gorm:"default:0"`
	LikeCount   int       `json:"like_count" gorm:"default:0"`
	AuthorID    uint      `json:"author_id" gorm:"not null"`
	ReviewerID  *uint     `json:"reviewer_id"`
	ReviewedAt  *time.Time `json:"reviewed_at"`
	PublishedAt *time.Time `json:"published_at"`
	Tags        string    `json:"tags" gorm:"type:text"`
	Metadata    KBMetadata `json:"metadata" gorm:"type:json"`

	// 关联
	Author   User  `json:"author" gorm:"foreignKey:AuthorID"`
	Reviewer *User `json:"reviewer,omitempty" gorm:"foreignKey:ReviewerID"`
}

// TableName 指定表名
func (KnowledgeBase) TableName() string {
	return "knowledge_bases"
}

// KBMetadata 知识库元数据
type KBMetadata map[string]interface{}

// Value 实现 driver.Valuer 接口
func (m KBMetadata) Value() (driver.Value, error) {
	if len(m) == 0 {
		return "{}", nil
	}
	return json.Marshal(m)
}

// Scan 实现 sql.Scanner 接口
func (m *KBMetadata) Scan(value interface{}) error {
	if value == nil {
		*m = KBMetadata{}
		return nil
	}
	
	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("无法将值转换为[]byte")
	}
	
	return json.Unmarshal(bytes, m)
}

// Tag 标签模型
type Tag struct {
	ID          uint      `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
	Name        string    `json:"name" gorm:"size:50;not null;uniqueIndex"`
	DisplayName string    `json:"display_name" gorm:"size:100;not null"`
	Description string    `json:"description" gorm:"type:text"`
	Color       string    `json:"color" gorm:"size:7;default:#007bff"`
	Category    string    `json:"category" gorm:"size:50"`
	UsageCount  int       `json:"usage_count" gorm:"default:0"`
	CreatorID   uint      `json:"creator_id" gorm:"not null"`

	// 关联
	Creator User `json:"creator" gorm:"foreignKey:CreatorID"`
}

// TableName 指定表名
func (Tag) TableName() string {
	return "tags"
}

// KnowledgeTag 知识库标签关联模型
type KnowledgeTag struct {
	ID            uint `json:"id" gorm:"primarykey"`
	KnowledgeID   uint `json:"knowledge_id" gorm:"not null"`
	TagID         uint `json:"tag_id" gorm:"not null"`

	// 关联
	Knowledge KnowledgeBase `json:"knowledge" gorm:"foreignKey:KnowledgeID"`
	Tag       Tag           `json:"tag" gorm:"foreignKey:TagID"`
}

// TableName 指定表名
func (KnowledgeTag) TableName() string {
	return "knowledge_tags"
}

// Template 模板模型
type Template struct {
	ID          uint      `json:"id" gorm:"primarykey"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
	Name        string    `json:"name" gorm:"size:100;not null;uniqueIndex"`
	DisplayName string    `json:"display_name" gorm:"size:100;not null"`
	Description string    `json:"description" gorm:"type:text"`
	Type        string    `json:"type" gorm:"size:50;not null"`
	Category    string    `json:"category" gorm:"size:50;not null"`
	Content     string    `json:"content" gorm:"type:longtext;not null"`
	Variables   string    `json:"variables" gorm:"type:text"`
	IsActive    bool      `json:"is_active" gorm:"default:true"`
	UsageCount  int       `json:"usage_count" gorm:"default:0"`
	CreatorID   uint      `json:"creator_id" gorm:"not null"`

	// 关联
	Creator User `json:"creator" gorm:"foreignKey:CreatorID"`
}

// TableName 指定表名
func (Template) TableName() string {
	return "templates"
}

// KnowledgeCreateRequest 知识库创建请求
type KnowledgeCreateRequest struct {
	Title    string     `json:"title" binding:"required,min=1,max=255"`
	Content  string     `json:"content" binding:"required,min=1"`
	Summary  string     `json:"summary"`
	Category string     `json:"category" binding:"required,oneof=vulnerability security tool guide best_practice"`
	Type     string     `json:"type" binding:"required,oneof=article tutorial faq solution reference"`
	Severity string     `json:"severity" binding:"omitempty,oneof=low medium high critical"`
	Tags     []string   `json:"tags"`
	Metadata KBMetadata `json:"metadata"`
}

// KnowledgeUpdateRequest 知识库更新请求
type KnowledgeUpdateRequest struct {
	Title    string     `json:"title" binding:"omitempty,min=1,max=255"`
	Content  string     `json:"content" binding:"omitempty,min=1"`
	Summary  string     `json:"summary"`
	Category string     `json:"category" binding:"omitempty,oneof=vulnerability security tool guide best_practice"`
	Type     string     `json:"type" binding:"omitempty,oneof=article tutorial faq solution reference"`
	Severity string     `json:"severity" binding:"omitempty,oneof=low medium high critical"`
	Status   string     `json:"status" binding:"omitempty,oneof=draft published archived"`
	Tags     []string   `json:"tags"`
	Metadata KBMetadata `json:"metadata"`
}

// KnowledgeSearchRequest 知识库搜索请求
type KnowledgeSearchRequest struct {
	Keyword  string `json:"keyword" form:"keyword"`
	Category string `json:"category" form:"category"`
	Type     string `json:"type" form:"type"`
	Severity string `json:"severity" form:"severity"`
	Status   string `json:"status" form:"status"`
	AuthorID string `json:"author_id" form:"author_id"`
	Tags     string `json:"tags" form:"tags"`
	SortBy   string `json:"sort_by" form:"sort_by"`
	SortDesc bool   `json:"sort_desc" form:"sort_desc"`
	PaginationRequest
}

// TagCreateRequest 标签创建请求
type TagCreateRequest struct {
	Name        string `json:"name" binding:"required,min=1,max=50"`
	DisplayName string `json:"display_name" binding:"required,min=1,max=100"`
	Description string `json:"description"`
	Color       string `json:"color" binding:"omitempty,len=7"`
	Category    string `json:"category" binding:"omitempty,oneof=vulnerability security tool language framework"`
}

// TagUpdateRequest 标签更新请求
type TagUpdateRequest struct {
	DisplayName string `json:"display_name" binding:"omitempty,min=1,max=100"`
	Description string `json:"description"`
	Color       string `json:"color" binding:"omitempty,len=7"`
	Category    string `json:"category" binding:"omitempty,oneof=vulnerability security tool language framework"`
}

// TemplateCreateRequest 模板创建请求
type TemplateCreateRequest struct {
	Name        string `json:"name" binding:"required,min=1,max=100"`
	DisplayName string `json:"display_name" binding:"required,min=1,max=100"`
	Description string `json:"description"`
	Type        string `json:"type" binding:"required,oneof=vulnerability_report security_guide fix_solution test_case"`
	Category    string `json:"category" binding:"required,oneof=report guide solution test"`
	Content     string `json:"content" binding:"required,min=1"`
	Variables   string `json:"variables"`
}

// TemplateUpdateRequest 模板更新请求
type TemplateUpdateRequest struct {
	DisplayName string `json:"display_name" binding:"omitempty,min=1,max=100"`
	Description string `json:"description"`
	Content     string `json:"content" binding:"omitempty,min=1"`
	Variables   string `json:"variables"`
	IsActive    *bool  `json:"is_active"`
}

// KnowledgeInfo 知识库信息（用于响应）
type KnowledgeInfo struct {
	ID          uint       `json:"id"`
	CreatedAt   time.Time  `json:"created_at"`
	UpdatedAt   time.Time  `json:"updated_at"`
	Title       string     `json:"title"`
	Content     string     `json:"content"`
	Summary     string     `json:"summary"`
	Category    string     `json:"category"`
	CategoryText string    `json:"category_text"`
	Type        string     `json:"type"`
	TypeText    string     `json:"type_text"`
	Severity    string     `json:"severity"`
	SeverityText string    `json:"severity_text"`
	Status      string     `json:"status"`
	StatusText  string     `json:"status_text"`
	ViewCount   int        `json:"view_count"`
	LikeCount   int        `json:"like_count"`
	AuthorID    uint       `json:"author_id"`
	ReviewerID  *uint      `json:"reviewer_id"`
	ReviewedAt  *time.Time `json:"reviewed_at"`
	PublishedAt *time.Time `json:"published_at"`
	Tags        []TagInfo  `json:"tags"`
	Metadata    KBMetadata `json:"metadata"`
	Author      UserInfo   `json:"author"`
	Reviewer    *UserInfo  `json:"reviewer,omitempty"`
}

// ToKnowledgeInfo 转换为知识库信息
func (k *KnowledgeBase) ToKnowledgeInfo() KnowledgeInfo {
	info := KnowledgeInfo{
		ID:           k.ID,
		CreatedAt:    k.CreatedAt,
		UpdatedAt:    k.UpdatedAt,
		Title:        k.Title,
		Content:      k.Content,
		Summary:      k.Summary,
		Category:     k.Category,
		CategoryText: k.GetCategoryText(),
		Type:         k.Type,
		TypeText:     k.GetTypeText(),
		Severity:     k.Severity,
		SeverityText: k.GetSeverityText(),
		Status:       k.Status,
		StatusText:   k.GetStatusText(),
		ViewCount:    k.ViewCount,
		LikeCount:    k.LikeCount,
		AuthorID:     k.AuthorID,
		ReviewerID:   k.ReviewerID,
		ReviewedAt:   k.ReviewedAt,
		PublishedAt:  k.PublishedAt,
		Metadata:     k.Metadata,
		Author:       k.Author.ToUserInfo(),
	}
	
	if k.Reviewer != nil {
		reviewerInfo := k.Reviewer.ToUserInfo()
		info.Reviewer = &reviewerInfo
	}
	
	return info
}

// GetCategoryText 获取分类文本
func (k *KnowledgeBase) GetCategoryText() string {
	switch k.Category {
	case "vulnerability":
		return "漏洞知识"
	case "security":
		return "安全知识"
	case "tool":
		return "工具使用"
	case "guide":
		return "操作指南"
	case "best_practice":
		return "最佳实践"
	default:
		return "未知分类"
	}
}

// GetTypeText 获取类型文本
func (k *KnowledgeBase) GetTypeText() string {
	switch k.Type {
	case "article":
		return "文章"
	case "tutorial":
		return "教程"
	case "faq":
		return "常见问题"
	case "solution":
		return "解决方案"
	case "reference":
		return "参考资料"
	default:
		return "未知类型"
	}
}

// GetSeverityText 获取严重程度文本
func (k *KnowledgeBase) GetSeverityText() string {
	switch k.Severity {
	case "low":
		return "低"
	case "medium":
		return "中"
	case "high":
		return "高"
	case "critical":
		return "严重"
	default:
		return ""
	}
}

// GetStatusText 获取状态文本
func (k *KnowledgeBase) GetStatusText() string {
	switch k.Status {
	case "draft":
		return "草稿"
	case "published":
		return "已发布"
	case "archived":
		return "已归档"
	default:
		return "未知状态"
	}
}

// TagInfo 标签信息（用于响应）
type TagInfo struct {
	ID          uint      `json:"id"`
	Name        string    `json:"name"`
	DisplayName string    `json:"display_name"`
	Description string    `json:"description"`
	Color       string    `json:"color"`
	Category    string    `json:"category"`
	UsageCount  int       `json:"usage_count"`
	CreatedAt   time.Time `json:"created_at"`
}

// ToTagInfo 转换为标签信息
func (t *Tag) ToTagInfo() TagInfo {
	return TagInfo{
		ID:          t.ID,
		Name:        t.Name,
		DisplayName: t.DisplayName,
		Description: t.Description,
		Color:       t.Color,
		Category:    t.Category,
		UsageCount:  t.UsageCount,
		CreatedAt:   t.CreatedAt,
	}
}

// KnowledgeStats 知识库统计
type KnowledgeStats struct {
	Total       int64                `json:"total"`
	Published   int64                `json:"published"`
	Draft       int64                `json:"draft"`
	ByCategory  map[string]int64     `json:"by_category"`
	ByType      map[string]int64     `json:"by_type"`
	TopTags     []TagStats           `json:"top_tags"`
	TopAuthors  []AuthorStats        `json:"top_authors"`
	Recent      []KnowledgeSummary   `json:"recent"`
}

// TagStats 标签统计
type TagStats struct {
	ID          uint   `json:"id"`
	Name        string `json:"name"`
	DisplayName string `json:"display_name"`
	Color       string `json:"color"`
	Count       int64  `json:"count"`
}

// AuthorStats 作者统计
type AuthorStats struct {
	ID       uint   `json:"id"`
	Username string `json:"username"`
	RealName string `json:"real_name"`
	Count    int64  `json:"count"`
}

// KnowledgeSummary 知识库摘要
type KnowledgeSummary struct {
	ID          uint      `json:"id"`
	Title       string    `json:"title"`
	Category    string    `json:"category"`
	Type        string    `json:"type"`
	ViewCount   int       `json:"view_count"`
	LikeCount   int       `json:"like_count"`
	CreatedAt   time.Time `json:"created_at"`
	AuthorName  string    `json:"author_name"`
}

// 知识库分类常量
const (
	KBCategoryVulnerability = "vulnerability"
	KBCategorySecurity      = "security"
	KBCategoryTool          = "tool"
	KBCategoryGuide         = "guide"
	KBCategoryBestPractice  = "best_practice"
)

// 知识库类型常量
const (
	KBTypeArticle   = "article"
	KBTypeTutorial  = "tutorial"
	KBTypeFAQ       = "faq"
	KBTypeSolution  = "solution"
	KBTypeReference = "reference"
)

// 知识库状态常量
const (
	KBStatusDraft     = "draft"
	KBStatusPublished = "published"
	KBStatusArchived  = "archived"
)

// 模板类型常量
const (
	TemplateTypeVulnerabilityReport = "vulnerability_report"
	TemplateTypeSecurityGuide       = "security_guide"
	TemplateTypeFixSolution         = "fix_solution"
	TemplateTypeTestCase            = "test_case"
)
