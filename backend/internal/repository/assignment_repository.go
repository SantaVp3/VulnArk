package repository

import (
	"errors"
	"strconv"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// AssignmentRepository 分配仓储接口
type AssignmentRepository interface {
	CreateAssignment(assignment *model.VulnerabilityAssignment) error
	GetAssignmentsByVulnerabilityID(vulnID uint) ([]*model.VulnerabilityAssignment, error)
	GetAssignmentsByAssigneeID(assigneeID uint) ([]*model.VulnerabilityAssignment, error)
	GetAssignmentStats() (*model.AssignmentStats, error)
}

// assignmentRepository 分配仓储实现
type assignmentRepository struct {
	db *gorm.DB
}

// NewAssignmentRepository 创建任务分配仓储
func NewAssignmentRepository(db *gorm.DB) AssignmentRepository {
	return &assignmentRepository{
		db: db,
	}
}

// CreateAssignment 创建分配记录
func (r *assignmentRepository) CreateAssignment(assignment *model.VulnerabilityAssignment) error {
	return r.db.Create(assignment).Error
}

// GetAssignmentsByVulnerabilityID 根据漏洞ID获取分配记录
func (r *assignmentRepository) GetAssignmentsByVulnerabilityID(vulnID uint) ([]*model.VulnerabilityAssignment, error) {
	var assignments []*model.VulnerabilityAssignment
	err := r.db.Preload("Assignee").Preload("Assigner").
		Where("vulnerability_id = ?", vulnID).
		Order("assigned_at DESC").
		Find(&assignments).Error
	return assignments, err
}

// GetAssignmentsByAssigneeID 根据分配人ID获取分配记录
func (r *assignmentRepository) GetAssignmentsByAssigneeID(assigneeID uint) ([]*model.VulnerabilityAssignment, error) {
	var assignments []*model.VulnerabilityAssignment
	err := r.db.Preload("Vulnerability").Preload("Assigner").
		Where("assignee_id = ?", assigneeID).
		Order("assigned_at DESC").
		Find(&assignments).Error
	return assignments, err
}

// GetAssignmentStats 获取分配统计
func (r *assignmentRepository) GetAssignmentStats() (*model.AssignmentStats, error) {
	stats := &model.AssignmentStats{
		ByAssignee: make(map[string]int64),
		ByRuleType: make(map[string]int64),
	}

	// 总分配数
	if err := r.db.Model(&model.VulnerabilityAssignment{}).Count(&stats.TotalAssignments).Error; err != nil {
		return nil, err
	}

	// 自动分配数
	if err := r.db.Model(&model.VulnerabilityAssignment{}).
		Where("assignment_type = ?", model.AssignmentTypeAuto).
		Count(&stats.AutoAssignments).Error; err != nil {
		return nil, err
	}

	// 手动分配数
	stats.ManualAssignments = stats.TotalAssignments - stats.AutoAssignments

	// 按分配人统计
	var assigneeStats []struct {
		AssigneeName string
		Count        int64
	}
	if err := r.db.Table("vulnerability_assignments va").
		Select("u.real_name as assignee_name, COUNT(*) as count").
		Joins("LEFT JOIN users u ON va.assignee_id = u.id").
		Group("u.real_name").
		Scan(&assigneeStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range assigneeStats {
		stats.ByAssignee[stat.AssigneeName] = stat.Count
	}

	// 最近分配记录
	var recentAssignments []model.AssignmentHistoryItem
	if err := r.db.Table("vulnerability_assignments va").
		Select("va.id, va.vulnerability_id, v.title as vulnerability_title, va.assignee_id, u1.real_name as assignee_name, va.assigner_id, u2.real_name as assigner_name, va.assignment_type, va.assigned_at").
		Joins("LEFT JOIN vulnerabilities v ON va.vulnerability_id = v.id").
		Joins("LEFT JOIN users u1 ON va.assignee_id = u1.id").
		Joins("LEFT JOIN users u2 ON va.assigner_id = u2.id").
		Order("va.assigned_at DESC").
		Limit(10).
		Scan(&recentAssignments).Error; err != nil {
		return nil, err
	}
	stats.RecentAssignments = recentAssignments

	return stats, nil
}

// AssignmentRuleRepository 分配规则仓储接口
type AssignmentRuleRepository interface {
	Create(rule *model.AssignmentRule) error
	GetByID(id uint) (*model.AssignmentRule, error)
	Update(rule *model.AssignmentRule) error
	Delete(id uint) error
	List(req *model.AssignmentRuleSearchRequest) ([]*model.AssignmentRule, int64, error)
	GetActiveRules() ([]*model.AssignmentRule, error)
	GetRulesByType(ruleType string) ([]*model.AssignmentRule, error)
}

// assignmentRuleRepository 分配规则仓储实现
type assignmentRuleRepository struct {
	db *gorm.DB
}

// NewAssignmentRuleRepository 创建分配规则仓储
func NewAssignmentRuleRepository(db *gorm.DB) AssignmentRuleRepository {
	return &assignmentRuleRepository{
		db: db,
	}
}

// Create 创建分配规则
func (r *assignmentRuleRepository) Create(rule *model.AssignmentRule) error {
	return r.db.Create(rule).Error
}

// GetByID 根据ID获取分配规则
func (r *assignmentRuleRepository) GetByID(id uint) (*model.AssignmentRule, error) {
	var rule model.AssignmentRule
	err := r.db.Preload("Assignee").First(&rule, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("分配规则不存在")
		}
		return nil, err
	}
	return &rule, nil
}

// Update 更新分配规则
func (r *assignmentRuleRepository) Update(rule *model.AssignmentRule) error {
	return r.db.Save(rule).Error
}

// Delete 删除分配规则
func (r *assignmentRuleRepository) Delete(id uint) error {
	return r.db.Delete(&model.AssignmentRule{}, id).Error
}

// List 获取分配规则列表
func (r *assignmentRuleRepository) List(req *model.AssignmentRuleSearchRequest) ([]*model.AssignmentRule, int64, error) {
	var rules []*model.AssignmentRule
	var total int64

	query := r.db.Model(&model.AssignmentRule{}).Preload("Assignee")

	// 搜索条件
	if req.Keyword != "" {
		query = query.Where("name LIKE ?", "%"+req.Keyword+"%")
	}

	if req.RuleType != "" {
		query = query.Where("rule_type = ?", req.RuleType)
	}

	if req.AssigneeID != "" {
		if assigneeID, err := strconv.ParseUint(req.AssigneeID, 10, 32); err == nil {
			query = query.Where("assignee_id = ?", uint(assigneeID))
		}
	}

	if req.IsActive != "" {
		if isActive, err := strconv.ParseBool(req.IsActive); err == nil {
			query = query.Where("is_active = ?", isActive)
		}
	}

	// 获取总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 分页
	offset := (req.Page - 1) * req.PageSize
	if err := query.Offset(offset).Limit(req.PageSize).Order("priority DESC, created_at DESC").Find(&rules).Error; err != nil {
		return nil, 0, err
	}

	return rules, total, nil
}

// GetActiveRules 获取激活的分配规则
func (r *assignmentRuleRepository) GetActiveRules() ([]*model.AssignmentRule, error) {
	var rules []*model.AssignmentRule
	err := r.db.Preload("Assignee").
		Where("is_active = ?", true).
		Order("priority DESC, created_at ASC").
		Find(&rules).Error
	return rules, err
}

// GetRulesByType 根据类型获取分配规则
func (r *assignmentRuleRepository) GetRulesByType(ruleType string) ([]*model.AssignmentRule, error) {
	var rules []*model.AssignmentRule
	err := r.db.Preload("Assignee").
		Where("rule_type = ? AND is_active = ?", ruleType, true).
		Order("priority DESC").
		Find(&rules).Error
	return rules, err
}

// TimelineRepository 时间线仓储接口
type TimelineRepository interface {
	Create(timeline *model.VulnerabilityTimeline) error
	GetByVulnerabilityID(vulnID uint) ([]*model.VulnerabilityTimeline, error)
	CreateTimelineEntry(vulnID uint, action, description, oldValue, newValue string, userID uint) error
}

// timelineRepository 时间线仓储实现
type timelineRepository struct {
	db *gorm.DB
}

// NewTimelineRepository 创建时间线仓储
func NewTimelineRepository(db *gorm.DB) TimelineRepository {
	return &timelineRepository{
		db: db,
	}
}

// Create 创建时间线记录
func (r *timelineRepository) Create(timeline *model.VulnerabilityTimeline) error {
	return r.db.Create(timeline).Error
}

// GetByVulnerabilityID 根据漏洞ID获取时间线
func (r *timelineRepository) GetByVulnerabilityID(vulnID uint) ([]*model.VulnerabilityTimeline, error) {
	var timeline []*model.VulnerabilityTimeline
	err := r.db.Preload("User").
		Where("vulnerability_id = ?", vulnID).
		Order("created_at ASC").
		Find(&timeline).Error
	return timeline, err
}

// CreateTimelineEntry 创建时间线条目
func (r *timelineRepository) CreateTimelineEntry(vulnID uint, action, description, oldValue, newValue string, userID uint) error {
	timeline := &model.VulnerabilityTimeline{
		VulnerabilityID: vulnID,
		Action:          action,
		Description:     description,
		OldValue:        oldValue,
		NewValue:        newValue,
		UserID:          userID,
	}
	return r.Create(timeline)
}
