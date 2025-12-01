package repository

import (
	"errors"
	"strconv"
	"strings"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// KnowledgeRepository 知识库仓储接口
type KnowledgeRepository interface {
	Create(knowledge *model.KnowledgeBase) error
	GetByID(id uint) (*model.KnowledgeBase, error)
	Update(knowledge *model.KnowledgeBase) error
	Delete(id uint) error
	List(req *model.KnowledgeSearchRequest) ([]*model.KnowledgeBase, int64, error)
	GetStats() (*model.KnowledgeStats, error)
	IncrementViewCount(id uint) error
	IncrementLikeCount(id uint) error
	GetPopular(limit int) ([]*model.KnowledgeBase, error)
	GetRecent(limit int) ([]*model.KnowledgeBase, error)
	Search(keyword string, limit int) ([]*model.KnowledgeBase, error)
	GetKnowledgeTags(knowledgeID uint) ([]*model.Tag, error)
	SetKnowledgeTags(knowledgeID uint, tagIDs []uint) error
}

// knowledgeRepository 知识库仓储实现
type knowledgeRepository struct {
	db *gorm.DB
}

// NewKnowledgeRepository 创建知识库仓储
func NewKnowledgeRepository(db *gorm.DB) KnowledgeRepository {
	return &knowledgeRepository{
		db: db,
	}
}

// Create 创建知识库
func (r *knowledgeRepository) Create(knowledge *model.KnowledgeBase) error {
	return r.db.Create(knowledge).Error
}

// GetByID 根据ID获取知识库
func (r *knowledgeRepository) GetByID(id uint) (*model.KnowledgeBase, error) {
	var knowledge model.KnowledgeBase
	err := r.db.Preload("Author").Preload("Reviewer").First(&knowledge, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("知识库不存在")
		}
		return nil, err
	}
	return &knowledge, nil
}

// Update 更新知识库
func (r *knowledgeRepository) Update(knowledge *model.KnowledgeBase) error {
	return r.db.Save(knowledge).Error
}

// Delete 删除知识库
func (r *knowledgeRepository) Delete(id uint) error {
	return r.db.Delete(&model.KnowledgeBase{}, id).Error
}

// List 获取知识库列表
func (r *knowledgeRepository) List(req *model.KnowledgeSearchRequest) ([]*model.KnowledgeBase, int64, error) {
	var knowledges []*model.KnowledgeBase
	var total int64

	query := r.db.Model(&model.KnowledgeBase{}).Preload("Author").Preload("Reviewer")

	// 搜索条件
	if req.Keyword != "" {
		query = query.Where("title LIKE ? OR content LIKE ? OR summary LIKE ?", 
			"%"+req.Keyword+"%", "%"+req.Keyword+"%", "%"+req.Keyword+"%")
	}

	if req.Category != "" {
		query = query.Where("category = ?", req.Category)
	}

	if req.Type != "" {
		query = query.Where("type = ?", req.Type)
	}

	if req.Severity != "" {
		query = query.Where("severity = ?", req.Severity)
	}

	if req.Status != "" {
		query = query.Where("status = ?", req.Status)
	}

	if req.AuthorID != "" {
		if authorID, err := strconv.ParseUint(req.AuthorID, 10, 32); err == nil {
			query = query.Where("author_id = ?", uint(authorID))
		}
	}

	if req.Tags != "" {
		// 标签搜索，支持多个标签用逗号分隔
		tags := strings.Split(req.Tags, ",")
		for _, tag := range tags {
			tag = strings.TrimSpace(tag)
			if tag != "" {
				query = query.Where("tags LIKE ?", "%"+tag+"%")
			}
		}
	}

	// 获取总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 排序
	orderBy := "created_at DESC"
	if req.SortBy != "" {
		switch req.SortBy {
		case "title", "created_at", "updated_at", "view_count", "like_count":
			if req.SortDesc {
				orderBy = req.SortBy + " DESC"
			} else {
				orderBy = req.SortBy + " ASC"
			}
		}
	}

	// 分页
	offset := (req.Page - 1) * req.PageSize
	if err := query.Offset(offset).Limit(req.PageSize).Order(orderBy).Find(&knowledges).Error; err != nil {
		return nil, 0, err
	}

	return knowledges, total, nil
}

// GetStats 获取知识库统计
func (r *knowledgeRepository) GetStats() (*model.KnowledgeStats, error) {
	stats := &model.KnowledgeStats{
		ByCategory: make(map[string]int64),
		ByType:     make(map[string]int64),
	}

	// 总数
	if err := r.db.Model(&model.KnowledgeBase{}).Count(&stats.Total).Error; err != nil {
		return nil, err
	}

	// 已发布数量
	if err := r.db.Model(&model.KnowledgeBase{}).Where("status = ?", "published").Count(&stats.Published).Error; err != nil {
		return nil, err
	}

	// 草稿数量
	if err := r.db.Model(&model.KnowledgeBase{}).Where("status = ?", "draft").Count(&stats.Draft).Error; err != nil {
		return nil, err
	}

	// 按分类统计
	var categoryStats []struct {
		Category string
		Count    int64
	}
	if err := r.db.Model(&model.KnowledgeBase{}).
		Select("category, COUNT(*) as count").
		Group("category").
		Scan(&categoryStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range categoryStats {
		stats.ByCategory[stat.Category] = stat.Count
	}

	// 按类型统计
	var typeStats []struct {
		Type  string
		Count int64
	}
	if err := r.db.Model(&model.KnowledgeBase{}).
		Select("type, COUNT(*) as count").
		Group("type").
		Scan(&typeStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range typeStats {
		stats.ByType[stat.Type] = stat.Count
	}

	// 热门标签
	var topTags []model.TagStats
	if err := r.db.Table("tags").
		Select("id, name, display_name, color, usage_count as count").
		Order("usage_count DESC").
		Limit(10).
		Scan(&topTags).Error; err != nil {
		return nil, err
	}
	stats.TopTags = topTags

	// 热门作者
	var topAuthors []model.AuthorStats
	if err := r.db.Table("knowledge_bases kb").
		Select("u.id, u.username, u.real_name, COUNT(*) as count").
		Joins("JOIN users u ON kb.author_id = u.id").
		Group("u.id, u.username, u.real_name").
		Order("count DESC").
		Limit(10).
		Scan(&topAuthors).Error; err != nil {
		return nil, err
	}
	stats.TopAuthors = topAuthors

	// 最近知识库
	var recent []model.KnowledgeSummary
	if err := r.db.Table("knowledge_bases kb").
		Select("kb.id, kb.title, kb.category, kb.type, kb.view_count, kb.like_count, kb.created_at, u.real_name as author_name").
		Joins("JOIN users u ON kb.author_id = u.id").
		Where("kb.status = ?", "published").
		Order("kb.created_at DESC").
		Limit(10).
		Scan(&recent).Error; err != nil {
		return nil, err
	}
	stats.Recent = recent

	return stats, nil
}

// IncrementViewCount 增加浏览次数
func (r *knowledgeRepository) IncrementViewCount(id uint) error {
	return r.db.Model(&model.KnowledgeBase{}).Where("id = ?", id).
		UpdateColumn("view_count", gorm.Expr("view_count + ?", 1)).Error
}

// IncrementLikeCount 增加点赞次数
func (r *knowledgeRepository) IncrementLikeCount(id uint) error {
	return r.db.Model(&model.KnowledgeBase{}).Where("id = ?", id).
		UpdateColumn("like_count", gorm.Expr("like_count + ?", 1)).Error
}

// GetPopular 获取热门知识库
func (r *knowledgeRepository) GetPopular(limit int) ([]*model.KnowledgeBase, error) {
	var knowledges []*model.KnowledgeBase
	err := r.db.Preload("Author").
		Where("status = ?", "published").
		Order("view_count DESC, like_count DESC").
		Limit(limit).
		Find(&knowledges).Error
	return knowledges, err
}

// GetRecent 获取最新知识库
func (r *knowledgeRepository) GetRecent(limit int) ([]*model.KnowledgeBase, error) {
	var knowledges []*model.KnowledgeBase
	err := r.db.Preload("Author").
		Where("status = ?", "published").
		Order("created_at DESC").
		Limit(limit).
		Find(&knowledges).Error
	return knowledges, err
}

// Search 搜索知识库
func (r *knowledgeRepository) Search(keyword string, limit int) ([]*model.KnowledgeBase, error) {
	var knowledges []*model.KnowledgeBase
	err := r.db.Preload("Author").
		Where("status = ? AND (title LIKE ? OR content LIKE ? OR summary LIKE ?)", 
			"published", "%"+keyword+"%", "%"+keyword+"%", "%"+keyword+"%").
		Order("view_count DESC").
		Limit(limit).
		Find(&knowledges).Error
	return knowledges, err
}

// TagRepository 标签仓储接口
type TagRepository interface {
	Create(tag *model.Tag) error
	GetByID(id uint) (*model.Tag, error)
	GetByName(name string) (*model.Tag, error)
	Update(tag *model.Tag) error
	Delete(id uint) error
	List() ([]*model.Tag, error)
	GetByCategory(category string) ([]*model.Tag, error)
	IncrementUsageCount(id uint) error
	DecrementUsageCount(id uint) error
	GetPopular(limit int) ([]*model.Tag, error)
}

// tagRepository 标签仓储实现
type tagRepository struct {
	db *gorm.DB
}

// NewTagRepository 创建标签仓储
func NewTagRepository(db *gorm.DB) TagRepository {
	return &tagRepository{
		db: db,
	}
}

// Create 创建标签
func (r *tagRepository) Create(tag *model.Tag) error {
	return r.db.Create(tag).Error
}

// GetByID 根据ID获取标签
func (r *tagRepository) GetByID(id uint) (*model.Tag, error) {
	var tag model.Tag
	err := r.db.Preload("Creator").First(&tag, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("标签不存在")
		}
		return nil, err
	}
	return &tag, nil
}

// GetByName 根据名称获取标签
func (r *tagRepository) GetByName(name string) (*model.Tag, error) {
	var tag model.Tag
	err := r.db.Where("name = ?", name).First(&tag).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("标签不存在")
		}
		return nil, err
	}
	return &tag, nil
}

// Update 更新标签
func (r *tagRepository) Update(tag *model.Tag) error {
	return r.db.Save(tag).Error
}

// Delete 删除标签
func (r *tagRepository) Delete(id uint) error {
	return r.db.Delete(&model.Tag{}, id).Error
}

// List 获取标签列表
func (r *tagRepository) List() ([]*model.Tag, error) {
	var tags []*model.Tag
	err := r.db.Preload("Creator").Order("usage_count DESC, name").Find(&tags).Error
	return tags, err
}

// GetByCategory 根据分类获取标签列表
func (r *tagRepository) GetByCategory(category string) ([]*model.Tag, error) {
	var tags []*model.Tag
	err := r.db.Where("category = ?", category).Order("usage_count DESC, name").Find(&tags).Error
	return tags, err
}

// IncrementUsageCount 增加使用次数
func (r *tagRepository) IncrementUsageCount(id uint) error {
	return r.db.Model(&model.Tag{}).Where("id = ?", id).
		UpdateColumn("usage_count", gorm.Expr("usage_count + ?", 1)).Error
}

// DecrementUsageCount 减少使用次数
func (r *tagRepository) DecrementUsageCount(id uint) error {
	return r.db.Model(&model.Tag{}).Where("id = ? AND usage_count > 0", id).
		UpdateColumn("usage_count", gorm.Expr("usage_count - ?", 1)).Error
}

// GetPopular 获取热门标签
func (r *tagRepository) GetPopular(limit int) ([]*model.Tag, error) {
	var tags []*model.Tag
	err := r.db.Order("usage_count DESC").Limit(limit).Find(&tags).Error
	return tags, err
}

// TemplateRepository 模板仓储接口
type TemplateRepository interface {
	Create(template *model.Template) error
	GetByID(id uint) (*model.Template, error)
	GetByName(name string) (*model.Template, error)
	Update(template *model.Template) error
	Delete(id uint) error
	List() ([]*model.Template, error)
	GetByType(templateType string) ([]*model.Template, error)
	GetByCategory(category string) ([]*model.Template, error)
	GetActive() ([]*model.Template, error)
	IncrementUsageCount(id uint) error
}

// templateRepository 模板仓储实现
type templateRepository struct {
	db *gorm.DB
}

// NewTemplateRepository 创建模板仓储
func NewTemplateRepository(db *gorm.DB) TemplateRepository {
	return &templateRepository{
		db: db,
	}
}

// Create 创建模板
func (r *templateRepository) Create(template *model.Template) error {
	return r.db.Create(template).Error
}

// GetByID 根据ID获取模板
func (r *templateRepository) GetByID(id uint) (*model.Template, error) {
	var template model.Template
	err := r.db.Preload("Creator").First(&template, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("模板不存在")
		}
		return nil, err
	}
	return &template, nil
}

// GetByName 根据名称获取模板
func (r *templateRepository) GetByName(name string) (*model.Template, error) {
	var template model.Template
	err := r.db.Where("name = ? AND is_active = ?", name, true).First(&template).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("模板不存在")
		}
		return nil, err
	}
	return &template, nil
}

// Update 更新模板
func (r *templateRepository) Update(template *model.Template) error {
	return r.db.Save(template).Error
}

// Delete 删除模板
func (r *templateRepository) Delete(id uint) error {
	return r.db.Delete(&model.Template{}, id).Error
}

// List 获取模板列表
func (r *templateRepository) List() ([]*model.Template, error) {
	var templates []*model.Template
	err := r.db.Preload("Creator").Order("usage_count DESC, created_at DESC").Find(&templates).Error
	return templates, err
}

// GetByType 根据类型获取模板列表
func (r *templateRepository) GetByType(templateType string) ([]*model.Template, error) {
	var templates []*model.Template
	err := r.db.Where("type = ? AND is_active = ?", templateType, true).
		Order("usage_count DESC").Find(&templates).Error
	return templates, err
}

// GetByCategory 根据分类获取模板列表
func (r *templateRepository) GetByCategory(category string) ([]*model.Template, error) {
	var templates []*model.Template
	err := r.db.Where("category = ? AND is_active = ?", category, true).
		Order("usage_count DESC").Find(&templates).Error
	return templates, err
}

// GetActive 获取激活的模板列表
func (r *templateRepository) GetActive() ([]*model.Template, error) {
	var templates []*model.Template
	err := r.db.Where("is_active = ?", true).Order("usage_count DESC").Find(&templates).Error
	return templates, err
}

// IncrementUsageCount 增加使用次数
func (r *templateRepository) IncrementUsageCount(id uint) error {
	return r.db.Model(&model.Template{}).Where("id = ?", id).
		UpdateColumn("usage_count", gorm.Expr("usage_count + ?", 1)).Error
}

// GetKnowledgeTags 获取知识库标签
func (r *knowledgeRepository) GetKnowledgeTags(knowledgeID uint) ([]*model.Tag, error) {
	var tags []*model.Tag
	err := r.db.Table("tags t").
		Select("t.*").
		Joins("JOIN knowledge_tags kt ON t.id = kt.tag_id").
		Where("kt.knowledge_id = ?", knowledgeID).
		Find(&tags).Error
	return tags, err
}

// SetKnowledgeTags 设置知识库标签
func (r *knowledgeRepository) SetKnowledgeTags(knowledgeID uint, tagIDs []uint) error {
	// 开启事务
	return r.db.Transaction(func(tx *gorm.DB) error {
		// 删除现有标签关联
		if err := tx.Where("knowledge_id = ?", knowledgeID).Delete(&model.KnowledgeTag{}).Error; err != nil {
			return err
		}

		// 添加新的标签关联
		for _, tagID := range tagIDs {
			knowledgeTag := &model.KnowledgeTag{
				KnowledgeID: knowledgeID,
				TagID:       tagID,
			}
			if err := tx.Create(knowledgeTag).Error; err != nil {
				return err
			}
		}

		return nil
	})
}
