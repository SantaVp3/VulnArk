package repository

import (
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"errors"
	"time"

	"gorm.io/gorm"
	"vulnark/internal/model"
)

// AIRepository AI仓储接口
type AIRepository interface {
	// AI配置管理
	GetAIConfiguration(userID uint) (*model.AIConfiguration, error)
	SaveAIConfiguration(config *model.AIConfiguration) error
	UpdateAIConfiguration(userID uint, config *model.AIConfigurationRequest) error
	DeleteAIConfiguration(userID uint) error
	
	// AI对话管理
	CreateConversation(userID uint, title string) (*model.AIConversation, error)
	GetConversation(userID uint, sessionID string) (*model.AIConversation, error)
	GetUserConversations(userID uint, limit, offset int) ([]model.AIConversation, int64, error)
	UpdateConversation(sessionID string, messages []model.AIMessage, tokensUsed int) error
	DeleteConversation(userID uint, sessionID string) error
	ArchiveConversation(userID uint, sessionID string) error
	
	// AI使用统计
	GetUsageStats(userID uint) (*model.AIUsageStats, error)
}

// aiRepository AI仓储实现
type aiRepository struct {
	db *gorm.DB
}

// generateSessionID 生成会话ID
func generateSessionID() string {
	bytes := make([]byte, 16)
	rand.Read(bytes)
	return hex.EncodeToString(bytes)
}

// NewAIRepository 创建AI仓储
func NewAIRepository(db *gorm.DB) AIRepository {
	return &aiRepository{
		db: db,
	}
}

// GetAIConfiguration 获取用户AI配置
func (r *aiRepository) GetAIConfiguration(userID uint) (*model.AIConfiguration, error) {
	var config model.AIConfiguration
	err := r.db.Where("user_id = ?", userID).First(&config).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil // 返回nil表示未配置
		}
		return nil, err
	}
	return &config, nil
}

// SaveAIConfiguration 保存AI配置
func (r *aiRepository) SaveAIConfiguration(config *model.AIConfiguration) error {
	return r.db.Save(config).Error
}

// UpdateAIConfiguration 更新AI配置
func (r *aiRepository) UpdateAIConfiguration(userID uint, req *model.AIConfigurationRequest) error {
	// 查找现有配置
	var config model.AIConfiguration
	err := r.db.Where("user_id = ?", userID).First(&config).Error
	
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			// 创建新配置
			config = model.AIConfiguration{
				UserID:      userID,
				Provider:    req.Provider,
				APIKey:      req.APIKey,
				APIURL:      req.APIURL,
				Model:       req.Model,
				MaxTokens:   req.MaxTokens,
				Temperature: req.Temperature,
				Timeout:     req.Timeout,
				Enabled:     req.Enabled,
			}
			return r.db.Create(&config).Error
		}
		return err
	}
	
	// 更新现有配置
	config.Provider = req.Provider
	config.APIKey = req.APIKey
	config.APIURL = req.APIURL
	config.Model = req.Model
	config.MaxTokens = req.MaxTokens
	config.Temperature = req.Temperature
	config.Timeout = req.Timeout
	config.Enabled = req.Enabled
	
	return r.db.Save(&config).Error
}

// DeleteAIConfiguration 删除AI配置
func (r *aiRepository) DeleteAIConfiguration(userID uint) error {
	return r.db.Where("user_id = ?", userID).Delete(&model.AIConfiguration{}).Error
}

// CreateConversation 创建新对话
func (r *aiRepository) CreateConversation(userID uint, title string) (*model.AIConversation, error) {
	sessionID := generateSessionID()
	
	conversation := &model.AIConversation{
		UserID:    userID,
		SessionID: sessionID,
		Title:     title,
		Messages:  "[]", // 空的JSON数组
		Status:    "active",
	}
	
	err := r.db.Create(conversation).Error
	if err != nil {
		return nil, err
	}
	
	return conversation, nil
}

// GetConversation 获取对话
func (r *aiRepository) GetConversation(userID uint, sessionID string) (*model.AIConversation, error) {
	var conversation model.AIConversation
	err := r.db.Where("user_id = ? AND session_id = ?", userID, sessionID).First(&conversation).Error
	if err != nil {
		return nil, err
	}
	return &conversation, nil
}

// GetUserConversations 获取用户对话列表
func (r *aiRepository) GetUserConversations(userID uint, limit, offset int) ([]model.AIConversation, int64, error) {
	var conversations []model.AIConversation
	var total int64
	
	// 获取总数
	err := r.db.Model(&model.AIConversation{}).Where("user_id = ? AND status != ?", userID, "deleted").Count(&total).Error
	if err != nil {
		return nil, 0, err
	}
	
	// 获取分页数据
	err = r.db.Where("user_id = ? AND status != ?", userID, "deleted").
		Order("updated_at DESC").
		Limit(limit).
		Offset(offset).
		Find(&conversations).Error
	
	return conversations, total, err
}

// UpdateConversation 更新对话消息
func (r *aiRepository) UpdateConversation(sessionID string, messages []model.AIMessage, tokensUsed int) error {
	messagesJSON, err := json.Marshal(messages)
	if err != nil {
		return err
	}
	
	return r.db.Model(&model.AIConversation{}).
		Where("session_id = ?", sessionID).
		Updates(map[string]interface{}{
			"messages":    string(messagesJSON),
			"tokens_used": tokensUsed,
			"updated_at":  time.Now(),
		}).Error
}

// DeleteConversation 删除对话
func (r *aiRepository) DeleteConversation(userID uint, sessionID string) error {
	return r.db.Where("user_id = ? AND session_id = ?", userID, sessionID).
		Update("status", "deleted").Error
}

// ArchiveConversation 归档对话
func (r *aiRepository) ArchiveConversation(userID uint, sessionID string) error {
	return r.db.Where("user_id = ? AND session_id = ?", userID, sessionID).
		Update("status", "archived").Error
}

// GetUsageStats 获取使用统计
func (r *aiRepository) GetUsageStats(userID uint) (*model.AIUsageStats, error) {
	var stats model.AIUsageStats

	// 总对话数
	var totalConversations int64
	err := r.db.Model(&model.AIConversation{}).
		Where("user_id = ? AND status != ?", userID, "deleted").
		Count(&totalConversations).Error
	if err != nil {
		return nil, err
	}
	stats.TotalConversations = int(totalConversations)

	// 活跃会话数
	var activeSessions int64
	err = r.db.Model(&model.AIConversation{}).
		Where("user_id = ? AND status = ?", userID, "active").
		Count(&activeSessions).Error
	if err != nil {
		return nil, err
	}
	stats.ActiveSessions = int(activeSessions)
	
	// 总消息数和token数（需要解析JSON）
	var conversations []model.AIConversation
	err = r.db.Select("messages, tokens_used").
		Where("user_id = ? AND status != ?", userID, "deleted").
		Find(&conversations).Error
	if err != nil {
		return nil, err
	}
	
	for _, conv := range conversations {
		var messages []model.AIMessage
		if err := json.Unmarshal([]byte(conv.Messages), &messages); err == nil {
			stats.TotalMessages += len(messages)
		}
		stats.TotalTokens += conv.TokensUsed
	}
	
	return &stats, nil
}
