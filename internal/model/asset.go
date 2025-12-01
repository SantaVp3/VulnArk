package model

import (
	"time"
)

// Asset 资产模型
type Asset struct {
	ID               uint      `json:"id" gorm:"primarykey"`
	CreatedAt        time.Time `json:"created_at"`
	UpdatedAt        time.Time `json:"updated_at"`
	Name             string    `json:"name" gorm:"size:100;not null" binding:"required"`
	Type             string    `json:"type" gorm:"size:50;not null" binding:"required"`
	Category         string    `json:"category" gorm:"size:50"`
	IPAddress        string    `json:"ip_address" gorm:"size:45"`
	Domain           string    `json:"domain" gorm:"size:255"`
	Port             string    `json:"port" gorm:"size:20"`
	OS               string    `json:"os" gorm:"size:100"`
	Version          string    `json:"version" gorm:"size:50"`
	Department       string    `json:"department" gorm:"size:100"`
	OwnerID          *uint     `json:"owner_id"`
	BusinessLine     string    `json:"business_line" gorm:"size:100"`
	ImportanceLevel  int       `json:"importance_level" gorm:"default:3"` // 1-高，2-中，3-低
	Description      string    `json:"description" gorm:"type:text"`
	Status           int       `json:"status" gorm:"default:1"` // 1-正常，0-下线

	// 关联
	Owner *User `json:"owner,omitempty" gorm:"foreignKey:OwnerID"`
}

// TableName 指定表名
func (Asset) TableName() string {
	return "assets"
}

// AssetCreateRequest 资产创建请求
type AssetCreateRequest struct {
	Name            string `json:"name" binding:"required,min=1,max=100"`
	Type            string `json:"type" binding:"required,oneof=server database application network"`
	Category        string `json:"category"`
	IPAddress       string `json:"ip_address" binding:"omitempty,ip"`
	Domain          string `json:"domain"`
	Port            string `json:"port"`
	OS              string `json:"os"`
	Version         string `json:"version"`
	Department      string `json:"department"`
	OwnerID         *uint  `json:"owner_id"`
	BusinessLine    string `json:"business_line"`
	ImportanceLevel int    `json:"importance_level" binding:"omitempty,oneof=1 2 3"`
	Description     string `json:"description"`
}

// AssetUpdateRequest 资产更新请求
type AssetUpdateRequest struct {
	Name            string `json:"name" binding:"omitempty,min=1,max=100"`
	Type            string `json:"type" binding:"omitempty,oneof=server database application network"`
	Category        string `json:"category"`
	IPAddress       string `json:"ip_address" binding:"omitempty,ip"`
	Domain          string `json:"domain"`
	Port            string `json:"port"`
	OS              string `json:"os"`
	Version         string `json:"version"`
	Department      string `json:"department"`
	OwnerID         *uint  `json:"owner_id"`
	BusinessLine    string `json:"business_line"`
	ImportanceLevel *int   `json:"importance_level" binding:"omitempty,oneof=1 2 3"`
	Description     string `json:"description"`
	Status          *int   `json:"status" binding:"omitempty,oneof=0 1"`
}

// AssetSearchRequest 资产搜索请求
type AssetSearchRequest struct {
	Keyword         string `json:"keyword" form:"keyword"`
	Type            string `json:"type" form:"type"`
	Category        string `json:"category" form:"category"`
	Department      string `json:"department" form:"department"`
	ImportanceLevel string `json:"importance_level" form:"importance_level"`
	Status          string `json:"status" form:"status"`
	OwnerID         string `json:"owner_id" form:"owner_id"`
	PaginationRequest
}

// AssetImportRequest 资产批量导入请求
type AssetImportRequest struct {
	Assets []AssetCreateRequest `json:"assets" binding:"required,dive"`
}

// AssetStats 资产统计
type AssetStats struct {
	Total           int64                    `json:"total"`
	ByType          map[string]int64         `json:"by_type"`
	ByDepartment    map[string]int64         `json:"by_department"`
	ByImportance    map[string]int64         `json:"by_importance"`
	ByStatus        map[string]int64         `json:"by_status"`
}

// AssetInfo 资产信息（用于响应）
type AssetInfo struct {
	ID               uint      `json:"id"`
	CreatedAt        time.Time `json:"created_at"`
	UpdatedAt        time.Time `json:"updated_at"`
	Name             string    `json:"name"`
	Type             string    `json:"type"`
	Category         string    `json:"category"`
	IPAddress        string    `json:"ip_address"`
	Domain           string    `json:"domain"`
	Port             string    `json:"port"`
	OS               string    `json:"os"`
	Version          string    `json:"version"`
	Department       string    `json:"department"`
	OwnerID          *uint     `json:"owner_id"`
	BusinessLine     string    `json:"business_line"`
	ImportanceLevel  int       `json:"importance_level"`
	Description      string    `json:"description"`
	Status           int       `json:"status"`
	Owner            *UserInfo `json:"owner,omitempty"`
}

// ToAssetInfo 转换为资产信息
func (a *Asset) ToAssetInfo() AssetInfo {
	info := AssetInfo{
		ID:              a.ID,
		CreatedAt:       a.CreatedAt,
		UpdatedAt:       a.UpdatedAt,
		Name:            a.Name,
		Type:            a.Type,
		Category:        a.Category,
		IPAddress:       a.IPAddress,
		Domain:          a.Domain,
		Port:            a.Port,
		OS:              a.OS,
		Version:         a.Version,
		Department:      a.Department,
		OwnerID:         a.OwnerID,
		BusinessLine:    a.BusinessLine,
		ImportanceLevel: a.ImportanceLevel,
		Description:     a.Description,
		Status:          a.Status,
	}

	if a.Owner != nil {
		ownerInfo := a.Owner.ToUserInfo()
		info.Owner = &ownerInfo
	}

	return info
}

// GetImportanceLevelText 获取重要性等级文本
func (a *Asset) GetImportanceLevelText() string {
	switch a.ImportanceLevel {
	case 1:
		return "高"
	case 2:
		return "中"
	case 3:
		return "低"
	default:
		return "未知"
	}
}

// GetStatusText 获取状态文本
func (a *Asset) GetStatusText() string {
	switch a.Status {
	case 1:
		return "正常"
	case 0:
		return "下线"
	default:
		return "未知"
	}
}

// IsActive 检查资产是否激活
func (a *Asset) IsActive() bool {
	return a.Status == 1
}

// 资产类型常量
const (
	AssetTypeServer      = "server"
	AssetTypeDatabase    = "database"
	AssetTypeApplication = "application"
	AssetTypeNetwork     = "network"
)

// 重要性等级常量
const (
	ImportanceLevelHigh   = 1
	ImportanceLevelMedium = 2
	ImportanceLevelLow    = 3
)

// 资产状态常量
const (
	AssetStatusOffline = 0
	AssetStatusOnline  = 1
)
