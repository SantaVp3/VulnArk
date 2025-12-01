package service

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"

	"vulnark/internal/config"
	"vulnark/internal/model"
	"vulnark/internal/repository"
)

// AIService AI服务接口
type AIService interface {
	// AI配置管理
	GetAIConfiguration(userID uint) (*model.AIConfiguration, error)
	SaveAIConfiguration(userID uint, req *model.AIConfigurationRequest) error
	TestAIConnection(req *model.AITestRequest) error
	GetSupportedProviders() []model.AIProvider
	
	// AI对话管理
	StartConversation(userID uint, title string) (*model.AIConversation, error)
	SendMessage(userID uint, req *model.AIChatRequest) (*model.AIChatResponse, error)
	GetConversations(userID uint, page, pageSize int) ([]model.AIConversation, int64, error)
	DeleteConversation(userID uint, sessionID string) error
	
	// AI使用统计
	GetUsageStats(userID uint) (*model.AIUsageStats, error)
}

// aiService AI服务实现
type aiService struct {
	aiRepo repository.AIRepository
	config *config.Config
}

// NewAIService 创建AI服务
func NewAIService(aiRepo repository.AIRepository) AIService {
	return &aiService{
		aiRepo: aiRepo,
		config: config.AppConfig,
	}
}

// GetAIConfiguration 获取AI配置
func (s *aiService) GetAIConfiguration(userID uint) (*model.AIConfiguration, error) {
	return s.aiRepo.GetAIConfiguration(userID)
}

// SaveAIConfiguration 保存AI配置
func (s *aiService) SaveAIConfiguration(userID uint, req *model.AIConfigurationRequest) error {
	// 验证提供商
	providers := model.GetSupportedProviders()
	var validProvider bool
	for _, p := range providers {
		if p.ID == req.Provider {
			validProvider = true
			break
		}
	}
	if !validProvider {
		return errors.New("不支持的AI提供商")
	}
	
	// 设置默认值
	if req.MaxTokens <= 0 {
		req.MaxTokens = 2000
	}
	if req.Temperature < 0 || req.Temperature > 2 {
		req.Temperature = 0.7
	}
	if req.Timeout <= 0 {
		req.Timeout = 30
	}
	
	return s.aiRepo.UpdateAIConfiguration(userID, req)
}

// TestAIConnection 测试AI连接
func (s *aiService) TestAIConnection(req *model.AITestRequest) error {
	if req.APIKey == "" {
		return errors.New("API密钥不能为空")
	}
	
	// 获取提供商配置
	providers := model.GetSupportedProviders()
	var provider *model.AIProvider
	for _, p := range providers {
		if p.ID == req.Provider {
			provider = &p
			break
		}
	}
	if provider == nil {
		return errors.New("不支持的AI提供商")
	}
	
	// 使用提供的URL或默认URL
	apiURL := req.APIURL
	if apiURL == "" {
		apiURL = provider.APIURL
	}
	
	// 使用提供的模型或默认模型
	model := req.Model
	if model == "" {
		model = provider.DefaultModel
	}
	
	// 构建测试消息
	testMessage := req.TestMessage
	if testMessage == "" {
		testMessage = "Hello, this is a connection test."
	}
	
	// 发送测试请求
	return s.sendTestRequest(apiURL, req.APIKey, model, testMessage, req.Provider)
}

// sendTestRequest 发送测试请求
func (s *aiService) sendTestRequest(apiURL, apiKey, model, message, provider string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	
	// 构建请求体
	var requestBody interface{}
	var endpoint string
	
	switch provider {
	case "openai", "deepseek":
		endpoint = "/chat/completions"
		requestBody = map[string]interface{}{
			"model": model,
			"messages": []map[string]string{
				{"role": "user", "content": message},
			},
			"max_tokens": 10,
		}
	case "qwen":
		endpoint = "/services/aigc/text-generation/generation"
		requestBody = map[string]interface{}{
			"model": model,
			"input": map[string]string{
				"messages": message,
			},
			"parameters": map[string]interface{}{
				"max_tokens": 10,
			},
		}
	default:
		// 自定义提供商，使用OpenAI格式
		endpoint = "/chat/completions"
		requestBody = map[string]interface{}{
			"model": model,
			"messages": []map[string]string{
				{"role": "user", "content": message},
			},
			"max_tokens": 10,
		}
	}
	
	jsonData, err := json.Marshal(requestBody)
	if err != nil {
		return fmt.Errorf("构建请求失败: %v", err)
	}
	
	// 创建HTTP请求
	req, err := http.NewRequestWithContext(ctx, "POST", apiURL+endpoint, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("创建请求失败: %v", err)
	}
	
	// 设置请求头
	req.Header.Set("Content-Type", "application/json")
	if provider == "qwen" {
		req.Header.Set("Authorization", "Bearer "+apiKey)
	} else {
		req.Header.Set("Authorization", "Bearer "+apiKey)
	}
	
	// 发送请求
	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("请求失败: %v", err)
	}
	defer resp.Body.Close()
	
	// 检查响应状态
	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("API请求失败 (状态码: %d): %s", resp.StatusCode, string(body))
	}
	
	return nil
}

// GetSupportedProviders 获取支持的提供商
func (s *aiService) GetSupportedProviders() []model.AIProvider {
	return model.GetSupportedProviders()
}

// StartConversation 开始新对话
func (s *aiService) StartConversation(userID uint, title string) (*model.AIConversation, error) {
	if title == "" {
		title = "新对话 - " + time.Now().Format("2006-01-02 15:04")
	}
	return s.aiRepo.CreateConversation(userID, title)
}

// SendMessage 发送消息
func (s *aiService) SendMessage(userID uint, req *model.AIChatRequest) (*model.AIChatResponse, error) {
	// 获取用户AI配置
	config, err := s.aiRepo.GetAIConfiguration(userID)
	if err != nil {
		return nil, fmt.Errorf("获取AI配置失败: %v", err)
	}

	if config == nil || !config.Enabled {
		return nil, errors.New("AI助手未配置或未启用，请先在设置中配置AI助手")
	}
	
	// 获取或创建对话
	var conversation *model.AIConversation
	if req.SessionID != "" {
		conversation, err = s.aiRepo.GetConversation(userID, req.SessionID)
		if err != nil {
			return nil, fmt.Errorf("获取对话失败: %v", err)
		}
	} else {
		// 创建新对话
		conversation, err = s.StartConversation(userID, "AI助手对话")
		if err != nil {
			return nil, fmt.Errorf("创建对话失败: %v", err)
		}
	}
	
	// 解析现有消息
	var messages []model.AIMessage
	if conversation.Messages != "" && conversation.Messages != "[]" {
		err = json.Unmarshal([]byte(conversation.Messages), &messages)
		if err != nil {
			return nil, fmt.Errorf("解析对话消息失败: %v", err)
		}
	}
	
	// 添加用户消息
	userMessage := model.AIMessage{
		Role:      "user",
		Content:   req.Message,
		Timestamp: time.Now(),
	}
	messages = append(messages, userMessage)
	
	// 调用AI API
	aiResponse, tokensUsed, err := s.callAIAPI(config, messages, req.Context)
	if err != nil {
		return nil, fmt.Errorf("AI API调用失败: %v", err)
	}
	
	// 添加AI响应
	aiMessage := model.AIMessage{
		Role:      "assistant",
		Content:   aiResponse,
		Timestamp: time.Now(),
		Tokens:    tokensUsed,
	}
	messages = append(messages, aiMessage)
	
	// 更新对话
	totalTokens := conversation.TokensUsed + tokensUsed
	err = s.aiRepo.UpdateConversation(conversation.SessionID, messages, totalTokens)
	if err != nil {
		return nil, fmt.Errorf("更新对话失败: %v", err)
	}
	
	// 生成建议问题
	suggestions := s.generateSuggestions(req.Context)
	
	return &model.AIChatResponse{
		SessionID:   conversation.SessionID,
		Message:     aiResponse,
		TokensUsed:  tokensUsed,
		Suggestions: suggestions,
	}, nil
}

// callAIAPI 调用AI API
func (s *aiService) callAIAPI(config *model.AIConfiguration, messages []model.AIMessage, contextInfo string) (string, int, error) {
	// 构建系统消息
	systemMessage := s.buildSystemMessage(contextInfo)
	
	// 构建API消息格式
	apiMessages := []map[string]string{
		{"role": "system", "content": systemMessage},
	}
	
	// 添加历史消息（限制数量以控制token使用）
	maxHistoryMessages := 10
	startIdx := 0
	if len(messages) > maxHistoryMessages {
		startIdx = len(messages) - maxHistoryMessages
	}
	
	for i := startIdx; i < len(messages); i++ {
		apiMessages = append(apiMessages, map[string]string{
			"role":    messages[i].Role,
			"content": messages[i].Content,
		})
	}
	
	// 构建请求
	requestBody := map[string]interface{}{
		"model":       config.Model,
		"messages":    apiMessages,
		"max_tokens":  config.MaxTokens,
		"temperature": config.Temperature,
	}
	
	jsonData, err := json.Marshal(requestBody)
	if err != nil {
		return "", 0, err
	}
	
	// 发送请求
	ctx, cancel := context.WithTimeout(context.Background(), time.Duration(config.Timeout)*time.Second)
	defer cancel()
	
	apiURL := config.APIURL
	if apiURL == "" {
		// 使用默认URL
		providers := model.GetSupportedProviders()
		for _, p := range providers {
			if p.ID == config.Provider {
				apiURL = p.APIURL
				break
			}
		}
	}
	
	req, err := http.NewRequestWithContext(ctx, "POST", apiURL+"/chat/completions", bytes.NewBuffer(jsonData))
	if err != nil {
		return "", 0, err
	}
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+config.APIKey)
	
	client := &http.Client{Timeout: time.Duration(config.Timeout) * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return "", 0, err
	}
	defer resp.Body.Close()
	
	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return "", 0, fmt.Errorf("API请求失败 (状态码: %d): %s", resp.StatusCode, string(body))
	}
	
	// 解析响应
	var response struct {
		Choices []struct {
			Message struct {
				Content string `json:"content"`
			} `json:"message"`
		} `json:"choices"`
		Usage struct {
			TotalTokens int `json:"total_tokens"`
		} `json:"usage"`
	}
	
	err = json.NewDecoder(resp.Body).Decode(&response)
	if err != nil {
		return "", 0, err
	}
	
	if len(response.Choices) == 0 {
		return "", 0, errors.New("AI响应为空")
	}
	
	return response.Choices[0].Message.Content, response.Usage.TotalTokens, nil
}

// buildSystemMessage 构建系统消息
func (s *aiService) buildSystemMessage(context string) string {
	baseMessage := `你是VulnArk漏洞管理系统的AI助手。你的主要职责是帮助用户：

1. 系统配置指导 - 协助用户正确配置系统设置
2. 语言和地区设置 - 提供本地化配置建议
3. 日期时间格式 - 推荐合适的时间格式设置
4. 系统管理支持 - 解答系统管理相关问题

请用简洁、专业的语言回答问题，并提供具体的操作建议。`

	if context != "" {
		baseMessage += "\n\n当前上下文信息：" + context
	}

	return baseMessage
}

// generateSuggestions 生成建议问题
func (s *aiService) generateSuggestions(context string) []string {
	suggestions := []string{
		"如何配置系统邮件通知？",
		"推荐的日期时间格式设置是什么？",
		"如何设置系统语言？",
		"系统性能优化建议？",
	}
	
	// 根据上下文调整建议
	if strings.Contains(context, "配置") {
		suggestions = []string{
			"配置完成后如何验证设置？",
			"有哪些常见的配置问题？",
			"如何备份当前配置？",
		}
	}
	
	return suggestions
}

// GetConversations 获取对话列表
func (s *aiService) GetConversations(userID uint, page, pageSize int) ([]model.AIConversation, int64, error) {
	offset := (page - 1) * pageSize
	return s.aiRepo.GetUserConversations(userID, pageSize, offset)
}

// DeleteConversation 删除对话
func (s *aiService) DeleteConversation(userID uint, sessionID string) error {
	return s.aiRepo.DeleteConversation(userID, sessionID)
}

// GetUsageStats 获取使用统计
func (s *aiService) GetUsageStats(userID uint) (*model.AIUsageStats, error) {
	return s.aiRepo.GetUsageStats(userID)
}
