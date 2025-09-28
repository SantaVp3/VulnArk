package model

import (
	"time"
)

// AIConfiguration AI配置模型
type AIConfiguration struct {
	ID          uint      `json:"id" gorm:"primaryKey"`
	UserID      uint      `json:"user_id" gorm:"not null;index"`
	Provider    string    `json:"provider" gorm:"size:50;not null;default:'openai'"`
	APIKey      string    `json:"api_key" gorm:"size:500"`
	APIURL      string    `json:"api_url" gorm:"size:255"`
	Model       string    `json:"model" gorm:"size:100"`
	MaxTokens   int       `json:"max_tokens" gorm:"default:2000"`
	Temperature float64   `json:"temperature" gorm:"default:0.7"`
	Timeout     int       `json:"timeout" gorm:"default:30"`
	Enabled     bool      `json:"enabled" gorm:"default:false"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
	
	// 关联用户
	User User `json:"user" gorm:"foreignKey:UserID"`
}

// AIConversation AI对话记录
type AIConversation struct {
	ID          uint      `json:"id" gorm:"primaryKey"`
	UserID      uint      `json:"user_id" gorm:"not null;index"`
	SessionID   string    `json:"session_id" gorm:"size:100;not null;index"`
	Title       string    `json:"title" gorm:"size:255"`
	Messages    string    `json:"messages" gorm:"type:longtext"` // JSON格式存储对话消息
	TokensUsed  int       `json:"tokens_used" gorm:"default:0"`
	Status      string    `json:"status" gorm:"size:20;default:'active'"` // active, archived, deleted
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
	
	// 关联用户
	User User `json:"user" gorm:"foreignKey:UserID"`
}

// AIMessage AI消息结构
type AIMessage struct {
	Role      string    `json:"role"`      // user, assistant, system
	Content   string    `json:"content"`
	Timestamp time.Time `json:"timestamp"`
	Tokens    int       `json:"tokens,omitempty"`
}

// AIProvider AI提供商信息
type AIProvider struct {
	ID           string   `json:"id" gorm:"primarykey"`
	Name         string   `json:"name" gorm:"size:100;not null"`
	Description  string   `json:"description" gorm:"size:255"`
	APIURL       string   `json:"api_url" gorm:"size:255"`
	Models       []string `json:"models" gorm:"type:json"`
	DefaultModel string   `json:"default_model" gorm:"size:100"`
	RequiresKey  bool     `json:"requires_key" gorm:"default:true"`
}

// GetSupportedProviders 获取支持的AI提供商列表
func GetSupportedProviders() []AIProvider {
	return []AIProvider{
		{
			ID:           "openai",
			Name:         "OpenAI",
			Description:  "OpenAI GPT系列模型",
			APIURL:       "https://api.openai.com/v1",
			Models:       []string{"gpt-3.5-turbo", "gpt-4", "gpt-4-turbo", "gpt-4o"},
			DefaultModel: "gpt-3.5-turbo",
			RequiresKey:  true,
		},
		{
			ID:           "deepseek",
			Name:         "DeepSeek",
			Description:  "DeepSeek AI模型",
			APIURL:       "https://api.deepseek.com/v1",
			Models:       []string{"deepseek-chat", "deepseek-coder"},
			DefaultModel: "deepseek-chat",
			RequiresKey:  true,
		},
		{
			ID:           "qwen",
			Name:         "通义千问",
			Description:  "阿里云通义千问模型",
			APIURL:       "https://dashscope.aliyuncs.com/api/v1",
			Models:       []string{"qwen-turbo", "qwen-plus", "qwen-max", "qwen-max-longcontext"},
			DefaultModel: "qwen-turbo",
			RequiresKey:  true,
		},
		{
			ID:           "custom",
			Name:         "自定义",
			Description:  "自定义AI服务提供商",
			APIURL:       "",
			Models:       []string{},
			DefaultModel: "",
			RequiresKey:  true,
		},
	}
}

// AIConfigurationRequest AI配置请求结构
type AIConfigurationRequest struct {
	Provider    string  `json:"provider" binding:"required"`
	APIKey      string  `json:"api_key"`
	APIURL      string  `json:"api_url"`
	Model       string  `json:"model"`
	MaxTokens   int     `json:"max_tokens"`
	Temperature float64 `json:"temperature"`
	Timeout     int     `json:"timeout"`
	Enabled     bool    `json:"enabled"`
}

// AITestRequest AI连接测试请求
type AITestRequest struct {
	Provider    string  `json:"provider" binding:"required"`
	APIKey      string  `json:"api_key" binding:"required"`
	APIURL      string  `json:"api_url"`
	Model       string  `json:"model"`
	TestMessage string  `json:"test_message"`
}

// AIChatRequest AI聊天请求
type AIChatRequest struct {
	SessionID string `json:"session_id"`
	Message   string `json:"message" binding:"required"`
	Context   string `json:"context"` // 系统上下文信息
}

// AIChatResponse AI聊天响应
type AIChatResponse struct {
	SessionID   string `json:"session_id"`
	Message     string `json:"message"`
	TokensUsed  int    `json:"tokens_used"`
	Suggestions []string `json:"suggestions,omitempty"` // 建议的后续问题
}

// AIUsageStats AI使用统计
type AIUsageStats struct {
	TotalConversations int `json:"total_conversations"`
	TotalMessages      int `json:"total_messages"`
	TotalTokens        int `json:"total_tokens"`
	ActiveSessions     int `json:"active_sessions"`
}
