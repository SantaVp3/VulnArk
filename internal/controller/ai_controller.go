package controller

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"

	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// AIController AI控制器
type AIController struct {
	aiService service.AIService
}

// NewAIController 创建AI控制器
func NewAIController(aiService service.AIService) *AIController {
	return &AIController{
		aiService: aiService,
	}
}

// GetAIConfiguration 获取AI配置
// @Summary 获取AI配置
// @Description 获取当前用户的AI助手配置
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} response.Response{data=model.AIConfiguration}
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/config [get]
func (c *AIController) GetAIConfiguration(ctx *gin.Context) {
	userID := getUserID(ctx)
	
	config, err := c.aiService.GetAIConfiguration(userID)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取AI配置失败")
		return
	}

	// 如果没有配置，返回默认配置
	if config == nil {
		config = &model.AIConfiguration{
			UserID:      userID,
			Provider:    "openai",
			MaxTokens:   2000,
			Temperature: 0.7,
			Timeout:     30,
			Enabled:     false,
		}
	}

	// 隐藏API密钥
	config.APIKey = ""

	utils.SuccessResponse(ctx, config)
}

// SaveAIConfiguration 保存AI配置
// @Summary 保存AI配置
// @Description 保存或更新用户的AI助手配置
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param config body model.AIConfigurationRequest true "AI配置"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/config [post]
func (c *AIController) SaveAIConfiguration(ctx *gin.Context) {
	userID := getUserID(ctx)
	
	var req model.AIConfigurationRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.BadRequestResponse(ctx, "请求参数错误")
		return
	}

	err := c.aiService.SaveAIConfiguration(userID, &req)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "保存AI配置失败")
		return
	}

	utils.SuccessResponse(ctx, "AI配置保存成功")
}

// TestAIConnection 测试AI连接
// @Summary 测试AI连接
// @Description 测试AI服务连接是否正常
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param test body model.AITestRequest true "测试请求"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/test [post]
func (c *AIController) TestAIConnection(ctx *gin.Context) {
	var req model.AITestRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.BadRequestResponse(ctx, "请求参数错误")
		return
	}

	err := c.aiService.TestAIConnection(&req)
	if err != nil {
		utils.BadRequestResponse(ctx, "连接测试失败")
		return
	}

	utils.SuccessResponse(ctx, "AI连接测试成功")
}

// GetSupportedProviders 获取支持的AI提供商
// @Summary 获取支持的AI提供商
// @Description 获取系统支持的AI服务提供商列表
// @Tags AI
// @Accept json
// @Produce json
// @Success 200 {object} response.Response{data=[]model.AIProvider}
// @Router /api/v1/ai/providers [get]
func (c *AIController) GetSupportedProviders(ctx *gin.Context) {
	providers := c.aiService.GetSupportedProviders()
	utils.SuccessResponse(ctx, providers)
}

// StartConversation 开始新对话
// @Summary 开始新对话
// @Description 创建一个新的AI对话会话
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body object{title=string} false "对话标题"
// @Success 200 {object} response.Response{data=model.AIConversation}
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/conversation [post]
func (c *AIController) StartConversation(ctx *gin.Context) {
	userID := getUserID(ctx)
	
	var req struct {
		Title string `json:"title"`
	}
	ctx.ShouldBindJSON(&req)
	
	conversation, err := c.aiService.StartConversation(userID, req.Title)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "创建对话失败")
		return
	}

	utils.SuccessResponse(ctx, conversation)
}

// SendMessage 发送消息
// @Summary 发送消息
// @Description 向AI助手发送消息并获取回复
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param message body model.AIChatRequest true "聊天请求"
// @Success 200 {object} response.Response{data=model.AIChatResponse}
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/chat [post]
func (c *AIController) SendMessage(ctx *gin.Context) {
	userID := getUserID(ctx)
	
	var req model.AIChatRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.BadRequestResponse(ctx, "请求参数错误")
		return
	}

	aiResponse, err := c.aiService.SendMessage(userID, &req)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "发送消息失败")
		return
	}

	utils.SuccessResponse(ctx, aiResponse)
}

// GetConversations 获取对话列表
// @Summary 获取对话列表
// @Description 获取用户的AI对话历史列表
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(20)
// @Success 200 {object} response.Response{data=response.PageResponse{items=[]model.AIConversation}}
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/conversations [get]
func (c *AIController) GetConversations(ctx *gin.Context) {
	userID := getUserID(ctx)
	
	page, _ := strconv.Atoi(ctx.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(ctx.DefaultQuery("page_size", "20"))
	
	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}
	
	conversations, total, err := c.aiService.GetConversations(userID, page, pageSize)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取对话列表失败")
		return
	}

	utils.PaginationResponse(ctx, total, page, pageSize, conversations)
}

// DeleteConversation 删除对话
// @Summary 删除对话
// @Description 删除指定的AI对话
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param session_id path string true "会话ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/conversation/{session_id} [delete]
func (c *AIController) DeleteConversation(ctx *gin.Context) {
	userID := getUserID(ctx)
	sessionID := ctx.Param("session_id")
	
	if sessionID == "" {
		utils.BadRequestResponse(ctx, "会话ID不能为空")
		return
	}

	err := c.aiService.DeleteConversation(userID, sessionID)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "删除对话失败")
		return
	}

	utils.SuccessResponse(ctx, "对话删除成功")
}

// GetUsageStats 获取使用统计
// @Summary 获取使用统计
// @Description 获取用户的AI助手使用统计信息
// @Tags AI
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} response.Response{data=model.AIUsageStats}
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/ai/stats [get]
func (c *AIController) GetUsageStats(ctx *gin.Context) {
	userID := getUserID(ctx)
	
	stats, err := c.aiService.GetUsageStats(userID)
	if err != nil {
		utils.ErrorResponse(ctx, http.StatusInternalServerError, "获取使用统计失败")
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// getUserID 从上下文获取用户ID
func getUserID(ctx *gin.Context) uint {
	userID, exists := ctx.Get("user_id")
	if !exists {
		return 0
	}
	
	if id, ok := userID.(uint); ok {
		return id
	}
	
	return 0
}
