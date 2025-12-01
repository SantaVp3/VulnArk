package repository

import (
	"errors"
	"strconv"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// AssetRepository 资产仓储接口
type AssetRepository interface {
	Create(asset *model.Asset) error
	GetByID(id uint) (*model.Asset, error)
	Update(asset *model.Asset) error
	Delete(id uint) error
	List(req *model.AssetSearchRequest) ([]*model.Asset, int64, error)
	GetStats() (*model.AssetStats, error)
	BatchCreate(assets []*model.Asset) error
	GetByName(name string) (*model.Asset, error)
	GetByIPAddress(ip string) (*model.Asset, error)
}

// assetRepository 资产仓储实现
type assetRepository struct {
	db *gorm.DB
}

// NewAssetRepository 创建资产仓储
func NewAssetRepository(db *gorm.DB) AssetRepository {
	return &assetRepository{
		db: db,
	}
}

// Create 创建资产
func (r *assetRepository) Create(asset *model.Asset) error {
	return r.db.Create(asset).Error
}

// GetByID 根据ID获取资产
func (r *assetRepository) GetByID(id uint) (*model.Asset, error) {
	var asset model.Asset
	err := r.db.Preload("Owner").First(&asset, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("资产不存在")
		}
		return nil, err
	}
	return &asset, nil
}

// Update 更新资产
func (r *assetRepository) Update(asset *model.Asset) error {
	return r.db.Save(asset).Error
}

// Delete 删除资产
func (r *assetRepository) Delete(id uint) error {
	return r.db.Delete(&model.Asset{}, id).Error
}

// List 获取资产列表
func (r *assetRepository) List(req *model.AssetSearchRequest) ([]*model.Asset, int64, error) {
	var assets []*model.Asset
	var total int64

	query := r.db.Model(&model.Asset{}).Preload("Owner")

	// 搜索条件
	if req.Keyword != "" {
		query = query.Where("name LIKE ? OR ip_address LIKE ? OR domain LIKE ? OR description LIKE ?",
			"%"+req.Keyword+"%", "%"+req.Keyword+"%", "%"+req.Keyword+"%", "%"+req.Keyword+"%")
	}

	if req.Type != "" {
		query = query.Where("type = ?", req.Type)
	}

	if req.Category != "" {
		query = query.Where("category = ?", req.Category)
	}

	if req.Department != "" {
		query = query.Where("department = ?", req.Department)
	}

	if req.ImportanceLevel != "" {
		if level, err := strconv.Atoi(req.ImportanceLevel); err == nil {
			query = query.Where("importance_level = ?", level)
		}
	}

	if req.Status != "" {
		if status, err := strconv.Atoi(req.Status); err == nil {
			query = query.Where("status = ?", status)
		}
	}

	if req.OwnerID != "" {
		if ownerID, err := strconv.ParseUint(req.OwnerID, 10, 32); err == nil {
			query = query.Where("owner_id = ?", uint(ownerID))
		}
	}

	// 获取总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 分页
	offset := (req.Page - 1) * req.PageSize
	if err := query.Offset(offset).Limit(req.PageSize).Order("created_at DESC").Find(&assets).Error; err != nil {
		return nil, 0, err
	}

	return assets, total, nil
}

// GetStats 获取资产统计
func (r *assetRepository) GetStats() (*model.AssetStats, error) {
	stats := &model.AssetStats{
		ByType:       make(map[string]int64),
		ByDepartment: make(map[string]int64),
		ByImportance: make(map[string]int64),
		ByStatus:     make(map[string]int64),
	}

	// 总数
	if err := r.db.Model(&model.Asset{}).Count(&stats.Total).Error; err != nil {
		return nil, err
	}

	// 按类型统计
	var typeStats []struct {
		Type  string
		Count int64
	}
	if err := r.db.Model(&model.Asset{}).Select("type, COUNT(*) as count").Group("type").Scan(&typeStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range typeStats {
		stats.ByType[stat.Type] = stat.Count
	}

	// 按部门统计
	var deptStats []struct {
		Department string
		Count      int64
	}
	if err := r.db.Model(&model.Asset{}).Select("department, COUNT(*) as count").Where("department != ''").Group("department").Scan(&deptStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range deptStats {
		stats.ByDepartment[stat.Department] = stat.Count
	}

	// 按重要性统计
	var importanceStats []struct {
		ImportanceLevel int
		Count           int64
	}
	if err := r.db.Model(&model.Asset{}).Select("importance_level, COUNT(*) as count").Group("importance_level").Scan(&importanceStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range importanceStats {
		var level string
		switch stat.ImportanceLevel {
		case 1:
			level = "高"
		case 2:
			level = "中"
		case 3:
			level = "低"
		default:
			level = "未知"
		}
		stats.ByImportance[level] = stat.Count
	}

	// 按状态统计
	var statusStats []struct {
		Status int
		Count  int64
	}
	if err := r.db.Model(&model.Asset{}).Select("status, COUNT(*) as count").Group("status").Scan(&statusStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range statusStats {
		var status string
		switch stat.Status {
		case 1:
			status = "正常"
		case 0:
			status = "下线"
		default:
			status = "未知"
		}
		stats.ByStatus[status] = stat.Count
	}

	return stats, nil
}

// BatchCreate 批量创建资产
func (r *assetRepository) BatchCreate(assets []*model.Asset) error {
	return r.db.CreateInBatches(assets, 100).Error
}

// GetByName 根据名称获取资产
func (r *assetRepository) GetByName(name string) (*model.Asset, error) {
	var asset model.Asset
	err := r.db.Where("name = ?", name).First(&asset).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &asset, nil
}

// GetByIPAddress 根据IP地址获取资产
func (r *assetRepository) GetByIPAddress(ip string) (*model.Asset, error) {
	var asset model.Asset
	err := r.db.Where("ip_address = ?", ip).First(&asset).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &asset, nil
}
