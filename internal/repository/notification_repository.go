package repository

import (
	"errors"
	"strconv"
	"time"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// NotificationRepository 通知仓储接口
type NotificationRepository interface {
	Create(notification *model.Notification) error
	GetByID(id uint) (*model.Notification, error)
	Update(notification *model.Notification) error
	Delete(id uint) error
	List(req *model.NotificationSearchRequest) ([]*model.Notification, int64, error)
	GetByRecipientID(recipientID uint, limit int) ([]*model.Notification, error)
	MarkAsRead(id uint) error
	MarkAllAsRead(recipientID uint) error
	GetUnreadCount(recipientID uint) (int64, error)
	GetStats(recipientID uint) (*model.NotificationStats, error)
	DeleteExpired() error
}

// notificationRepository 通知仓储实现
type notificationRepository struct {
	db *gorm.DB
}

// NewNotificationRepository 创建通知仓储
func NewNotificationRepository(db *gorm.DB) NotificationRepository {
	return &notificationRepository{
		db: db,
	}
}

// Create 创建通知
func (r *notificationRepository) Create(notification *model.Notification) error {
	return r.db.Create(notification).Error
}

// GetByID 根据ID获取通知
func (r *notificationRepository) GetByID(id uint) (*model.Notification, error) {
	var notification model.Notification
	err := r.db.Preload("Recipient").Preload("Sender").First(&notification, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("通知不存在")
		}
		return nil, err
	}
	return &notification, nil
}

// Update 更新通知
func (r *notificationRepository) Update(notification *model.Notification) error {
	return r.db.Save(notification).Error
}

// Delete 删除通知
func (r *notificationRepository) Delete(id uint) error {
	return r.db.Delete(&model.Notification{}, id).Error
}

// List 获取通知列表
func (r *notificationRepository) List(req *model.NotificationSearchRequest) ([]*model.Notification, int64, error) {
	var notifications []*model.Notification
	var total int64

	query := r.db.Model(&model.Notification{}).Preload("Recipient").Preload("Sender")

	// 搜索条件
	if req.Keyword != "" {
		query = query.Where("title LIKE ? OR content LIKE ?", "%"+req.Keyword+"%", "%"+req.Keyword+"%")
	}

	if req.Type != "" {
		query = query.Where("type = ?", req.Type)
	}

	if req.Level != "" {
		query = query.Where("level = ?", req.Level)
	}

	if req.IsRead != "" {
		if isRead, err := strconv.ParseBool(req.IsRead); err == nil {
			query = query.Where("is_read = ?", isRead)
		}
	}

	if req.RecipientID != "" {
		if recipientID, err := strconv.ParseUint(req.RecipientID, 10, 32); err == nil {
			query = query.Where("recipient_id = ?", uint(recipientID))
		}
	}

	// 日期范围
	if req.StartDate != "" {
		if startDate, err := time.Parse("2006-01-02", req.StartDate); err == nil {
			query = query.Where("created_at >= ?", startDate)
		}
	}

	if req.EndDate != "" {
		if endDate, err := time.Parse("2006-01-02", req.EndDate); err == nil {
			query = query.Where("created_at <= ?", endDate.Add(24*time.Hour))
		}
	}

	// 获取总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 分页
	offset := (req.Page - 1) * req.PageSize
	if err := query.Offset(offset).Limit(req.PageSize).Order("created_at DESC").Find(&notifications).Error; err != nil {
		return nil, 0, err
	}

	return notifications, total, nil
}

// GetByRecipientID 根据接收人ID获取通知列表
func (r *notificationRepository) GetByRecipientID(recipientID uint, limit int) ([]*model.Notification, error) {
	var notifications []*model.Notification
	query := r.db.Preload("Sender").
		Where("recipient_id = ?", recipientID).
		Order("created_at DESC")
	
	if limit > 0 {
		query = query.Limit(limit)
	}
	
	err := query.Find(&notifications).Error
	return notifications, err
}

// MarkAsRead 标记为已读
func (r *notificationRepository) MarkAsRead(id uint) error {
	now := time.Now()
	return r.db.Model(&model.Notification{}).
		Where("id = ? AND is_read = ?", id, false).
		Updates(map[string]interface{}{
			"is_read":    true,
			"read_at":    &now,
			"updated_at": now,
		}).Error
}

// MarkAllAsRead 标记所有通知为已读
func (r *notificationRepository) MarkAllAsRead(recipientID uint) error {
	now := time.Now()
	return r.db.Model(&model.Notification{}).
		Where("recipient_id = ? AND is_read = ?", recipientID, false).
		Updates(map[string]interface{}{
			"is_read":    true,
			"read_at":    &now,
			"updated_at": now,
		}).Error
}

// GetUnreadCount 获取未读通知数量
func (r *notificationRepository) GetUnreadCount(recipientID uint) (int64, error) {
	var count int64
	err := r.db.Model(&model.Notification{}).
		Where("recipient_id = ? AND is_read = ?", recipientID, false).
		Count(&count).Error
	return count, err
}

// GetStats 获取通知统计
func (r *notificationRepository) GetStats(recipientID uint) (*model.NotificationStats, error) {
	stats := &model.NotificationStats{
		ByType:  make(map[string]int64),
		ByLevel: make(map[string]int64),
	}

	// 总数
	if err := r.db.Model(&model.Notification{}).
		Where("recipient_id = ?", recipientID).
		Count(&stats.Total).Error; err != nil {
		return nil, err
	}

	// 未读数
	if err := r.db.Model(&model.Notification{}).
		Where("recipient_id = ? AND is_read = ?", recipientID, false).
		Count(&stats.Unread).Error; err != nil {
		return nil, err
	}

	// 按类型统计
	var typeStats []struct {
		Type  string
		Count int64
	}
	if err := r.db.Model(&model.Notification{}).
		Select("type, COUNT(*) as count").
		Where("recipient_id = ?", recipientID).
		Group("type").
		Scan(&typeStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range typeStats {
		stats.ByType[stat.Type] = stat.Count
	}

	// 按级别统计
	var levelStats []struct {
		Level string
		Count int64
	}
	if err := r.db.Model(&model.Notification{}).
		Select("level, COUNT(*) as count").
		Where("recipient_id = ?", recipientID).
		Group("level").
		Scan(&levelStats).Error; err != nil {
		return nil, err
	}
	for _, stat := range levelStats {
		stats.ByLevel[stat.Level] = stat.Count
	}

	// 最近通知
	var recentNotifications []model.NotificationSummary
	if err := r.db.Table("notifications n").
		Select("n.id, n.title, n.type, n.level, n.is_read, n.created_at, COALESCE(u.real_name, '系统') as sender_name").
		Joins("LEFT JOIN users u ON n.sender_id = u.id").
		Where("n.recipient_id = ?", recipientID).
		Order("n.created_at DESC").
		Limit(10).
		Scan(&recentNotifications).Error; err != nil {
		return nil, err
	}
	stats.Recent = recentNotifications

	return stats, nil
}

// DeleteExpired 删除过期通知
func (r *notificationRepository) DeleteExpired() error {
	return r.db.Where("expires_at IS NOT NULL AND expires_at < ?", time.Now()).
		Delete(&model.Notification{}).Error
}

// EmailTemplateRepository 邮件模板仓储接口
type EmailTemplateRepository interface {
	Create(template *model.EmailTemplate) error
	GetByID(id uint) (*model.EmailTemplate, error)
	GetByName(name string) (*model.EmailTemplate, error)
	Update(template *model.EmailTemplate) error
	Delete(id uint) error
	List() ([]*model.EmailTemplate, error)
	GetActiveTemplates() ([]*model.EmailTemplate, error)
}

// emailTemplateRepository 邮件模板仓储实现
type emailTemplateRepository struct {
	db *gorm.DB
}

// NewEmailTemplateRepository 创建邮件模板仓储
func NewEmailTemplateRepository(db *gorm.DB) EmailTemplateRepository {
	return &emailTemplateRepository{
		db: db,
	}
}

// Create 创建邮件模板
func (r *emailTemplateRepository) Create(template *model.EmailTemplate) error {
	return r.db.Create(template).Error
}

// GetByID 根据ID获取邮件模板
func (r *emailTemplateRepository) GetByID(id uint) (*model.EmailTemplate, error) {
	var template model.EmailTemplate
	err := r.db.Preload("Creator").First(&template, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("邮件模板不存在")
		}
		return nil, err
	}
	return &template, nil
}

// GetByName 根据名称获取邮件模板
func (r *emailTemplateRepository) GetByName(name string) (*model.EmailTemplate, error) {
	var template model.EmailTemplate
	err := r.db.Preload("Creator").Where("name = ? AND is_active = ?", name, true).First(&template).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("邮件模板不存在")
		}
		return nil, err
	}
	return &template, nil
}

// Update 更新邮件模板
func (r *emailTemplateRepository) Update(template *model.EmailTemplate) error {
	return r.db.Save(template).Error
}

// Delete 删除邮件模板
func (r *emailTemplateRepository) Delete(id uint) error {
	return r.db.Delete(&model.EmailTemplate{}, id).Error
}

// List 获取邮件模板列表
func (r *emailTemplateRepository) List() ([]*model.EmailTemplate, error) {
	var templates []*model.EmailTemplate
	err := r.db.Preload("Creator").Order("created_at DESC").Find(&templates).Error
	return templates, err
}

// GetActiveTemplates 获取激活的邮件模板列表
func (r *emailTemplateRepository) GetActiveTemplates() ([]*model.EmailTemplate, error) {
	var templates []*model.EmailTemplate
	err := r.db.Preload("Creator").Where("is_active = ?", true).Order("name").Find(&templates).Error
	return templates, err
}

// EmailLogRepository 邮件日志仓储接口
type EmailLogRepository interface {
	Create(log *model.EmailLog) error
	GetByID(id uint) (*model.EmailLog, error)
	List(limit int) ([]*model.EmailLog, error)
	UpdateStatus(id uint, status string, error string) error
}

// emailLogRepository 邮件日志仓储实现
type emailLogRepository struct {
	db *gorm.DB
}

// NewEmailLogRepository 创建邮件日志仓储
func NewEmailLogRepository(db *gorm.DB) EmailLogRepository {
	return &emailLogRepository{
		db: db,
	}
}

// Create 创建邮件日志
func (r *emailLogRepository) Create(log *model.EmailLog) error {
	return r.db.Create(log).Error
}

// GetByID 根据ID获取邮件日志
func (r *emailLogRepository) GetByID(id uint) (*model.EmailLog, error) {
	var log model.EmailLog
	err := r.db.Preload("Template").Preload("Sender").First(&log, id).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("邮件日志不存在")
		}
		return nil, err
	}
	return &log, nil
}

// List 获取邮件日志列表
func (r *emailLogRepository) List(limit int) ([]*model.EmailLog, error) {
	var logs []*model.EmailLog
	query := r.db.Preload("Template").Preload("Sender").Order("created_at DESC")
	
	if limit > 0 {
		query = query.Limit(limit)
	}
	
	err := query.Find(&logs).Error
	return logs, err
}

// UpdateStatus 更新邮件状态
func (r *emailLogRepository) UpdateStatus(id uint, status string, errorMsg string) error {
	updates := map[string]interface{}{
		"status": status,
	}
	
	if status == model.EmailStatusSent {
		now := time.Now()
		updates["sent_at"] = &now
	}
	
	if errorMsg != "" {
		updates["error"] = errorMsg
	}
	
	return r.db.Model(&model.EmailLog{}).Where("id = ?", id).Updates(updates).Error
}

// NotificationSettingRepository 通知设置仓储接口
type NotificationSettingRepository interface {
	GetByUserID(userID uint) (*model.NotificationSetting, error)
	CreateOrUpdate(setting *model.NotificationSetting) error
}

// notificationSettingRepository 通知设置仓储实现
type notificationSettingRepository struct {
	db *gorm.DB
}

// NewNotificationSettingRepository 创建通知设置仓储
func NewNotificationSettingRepository(db *gorm.DB) NotificationSettingRepository {
	return &notificationSettingRepository{
		db: db,
	}
}

// GetByUserID 根据用户ID获取通知设置
func (r *notificationSettingRepository) GetByUserID(userID uint) (*model.NotificationSetting, error) {
	var setting model.NotificationSetting
	err := r.db.Where("user_id = ?", userID).First(&setting).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			// 如果不存在，创建默认设置
			setting = model.NotificationSetting{
				UserID:                userID,
				EmailEnabled:          true,
				SystemEnabled:         true,
				VulnerabilityAssigned: true,
				VulnerabilityUpdated:  true,
				ReportSubmitted:       true,
				ReportReviewed:        true,
				SystemMaintenance:     true,
				SecurityAlert:         true,
			}
			if err := r.db.Create(&setting).Error; err != nil {
				return nil, err
			}
		} else {
			return nil, err
		}
	}
	return &setting, nil
}

// CreateOrUpdate 创建或更新通知设置
func (r *notificationSettingRepository) CreateOrUpdate(setting *model.NotificationSetting) error {
	return r.db.Save(setting).Error
}
