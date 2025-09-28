package repository

import (
	"gorm.io/gorm"
	"vulnark/internal/model"
)

// SettingsRepository 设置仓库接口
type SettingsRepository interface {
	// 系统设置
	GetSystemSettings(userID uint) (*model.SystemSettings, error)
	CreateSystemSettings(settings *model.SystemSettings) error
	UpdateSystemSettings(userID uint, settings *model.SystemSettings) error
	
	// 通知设置
	GetNotificationSettings(userID uint) (*model.NotificationSettings, error)
	CreateNotificationSettings(settings *model.NotificationSettings) error
	UpdateNotificationSettings(userID uint, settings *model.NotificationSettings) error
}

// settingsRepository 设置仓库实现
type settingsRepository struct {
	db *gorm.DB
}

// NewSettingsRepository 创建设置仓库
func NewSettingsRepository(db *gorm.DB) SettingsRepository {
	return &settingsRepository{
		db: db,
	}
}

// GetSystemSettings 获取系统设置
func (r *settingsRepository) GetSystemSettings(userID uint) (*model.SystemSettings, error) {
	var settings model.SystemSettings
	err := r.db.Where("user_id = ?", userID).First(&settings).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			// 如果没有设置记录，创建默认设置
			defaultSettings := &model.SystemSettings{
				UserID:     userID,
				Theme:      "system",
				Language:   "zh-CN",
				Timezone:   "Asia/Shanghai",
				DateFormat: "YYYY-MM-DD",
				TimeFormat: "24h",
			}
			if createErr := r.CreateSystemSettings(defaultSettings); createErr != nil {
				return nil, createErr
			}
			return defaultSettings, nil
		}
		return nil, err
	}
	return &settings, nil
}

// CreateSystemSettings 创建系统设置
func (r *settingsRepository) CreateSystemSettings(settings *model.SystemSettings) error {
	return r.db.Create(settings).Error
}

// UpdateSystemSettings 更新系统设置
func (r *settingsRepository) UpdateSystemSettings(userID uint, settings *model.SystemSettings) error {
	return r.db.Model(&model.SystemSettings{}).Where("user_id = ?", userID).Updates(settings).Error
}

// GetNotificationSettings 获取通知设置
func (r *settingsRepository) GetNotificationSettings(userID uint) (*model.NotificationSettings, error) {
	var settings model.NotificationSettings
	err := r.db.Where("user_id = ?", userID).First(&settings).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			// 如果没有设置记录，创建默认设置
			defaultSettings := &model.NotificationSettings{
				UserID:            userID,
				VulnNewEmail:      true,
				VulnNewPush:       true,
				VulnNewSMS:        false,
				VulnAssignedEmail: true,
				VulnAssignedPush:  true,
				VulnAssignedSMS:   true,
				VulnFixedEmail:    true,
				VulnFixedPush:     false,
				VulnFixedSMS:      false,
				ReportEmail:       true,
				ReportPush:        false,
				ReportSMS:         false,
				SystemEmail:       true,
				SystemPush:        true,
				SystemSMS:         false,
				SocialEmail:       false,
				SocialPush:        true,
				SocialSMS:         false,
			}
			if createErr := r.CreateNotificationSettings(defaultSettings); createErr != nil {
				return nil, createErr
			}
			return defaultSettings, nil
		}
		return nil, err
	}
	return &settings, nil
}

// CreateNotificationSettings 创建通知设置
func (r *settingsRepository) CreateNotificationSettings(settings *model.NotificationSettings) error {
	return r.db.Create(settings).Error
}

// UpdateNotificationSettings 更新通知设置
func (r *settingsRepository) UpdateNotificationSettings(userID uint, settings *model.NotificationSettings) error {
	return r.db.Model(&model.NotificationSettings{}).Where("user_id = ?", userID).Updates(settings).Error
}
