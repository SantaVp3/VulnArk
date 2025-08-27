package controller

import (
	"fmt"
	"strconv"

	"github.com/gin-gonic/gin"
	"vulnark/internal/model"
	"vulnark/internal/service"
	"vulnark/pkg/utils"
)

// SystemController 系统控制器
type SystemController struct {
	configService service.SystemConfigService
	auditService  service.AuditLogService
}

// NewSystemController 创建系统控制器
func NewSystemController() *SystemController {
	return &SystemController{
		configService: service.NewSystemConfigService(),
		auditService:  service.NewAuditLogService(),
	}
}

// GetSystemConfigs 获取系统配置列表
// @Summary 获取系统配置列表
// @Description 获取所有系统配置
// @Tags 系统管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=[]model.SystemConfig}
// @Failure 500 {object} model.Response
// @Router /api/v1/system/configs [get]
func (c *SystemController) GetSystemConfigs(ctx *gin.Context) {
	configs, err := c.configService.GetConfigList()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, configs)
}

// GetSystemConfig 获取系统配置详情
// @Summary 获取系统配置详情
// @Description 根据键获取系统配置详情
// @Tags 系统管理
// @Produce json
// @Security ApiKeyAuth
// @Param key path string true "配置键"
// @Success 200 {object} model.Response{data=model.SystemConfig}
// @Failure 400 {object} model.Response
// @Router /api/v1/system/configs/{key} [get]
func (c *SystemController) GetSystemConfig(ctx *gin.Context) {
	key := ctx.Param("key")
	if key == "" {
		utils.BadRequestResponse(ctx, "配置键不能为空")
		return
	}

	config, err := c.configService.GetConfigByKey(key)
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, config)
}

// UpdateSystemConfig 更新系统配置
// @Summary 更新系统配置
// @Description 更新系统配置值
// @Tags 系统管理
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param key path string true "配置键"
// @Param request body model.SystemConfigUpdateRequest true "更新请求"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/system/configs/{key} [put]
func (c *SystemController) UpdateSystemConfig(ctx *gin.Context) {
	key := ctx.Param("key")
	if key == "" {
		utils.BadRequestResponse(ctx, "配置键不能为空")
		return
	}

	var req model.SystemConfigUpdateRequest
	if err := utils.BindAndValidate(ctx, &req); err != nil {
		return
	}

	if err := c.configService.UpdateConfig(key, &req); err != nil {
		utils.BadRequestResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetConfigsByCategory 根据分类获取配置列表
// @Summary 根据分类获取配置列表
// @Description 根据分类获取系统配置列表
// @Tags 系统管理
// @Produce json
// @Security ApiKeyAuth
// @Param category path string true "配置分类"
// @Success 200 {object} model.Response{data=[]model.SystemConfig}
// @Failure 500 {object} model.Response
// @Router /api/v1/system/configs/category/{category} [get]
func (c *SystemController) GetConfigsByCategory(ctx *gin.Context) {
	category := ctx.Param("category")
	if category == "" {
		utils.BadRequestResponse(ctx, "配置分类不能为空")
		return
	}

	configs, err := c.configService.GetConfigsByCategory(category)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, configs)
}

// GetPublicConfigs 获取公开配置列表
// @Summary 获取公开配置列表
// @Description 获取可公开访问的系统配置
// @Tags 系统管理
// @Produce json
// @Success 200 {object} model.Response{data=[]model.SystemConfig}
// @Failure 500 {object} model.Response
// @Router /api/v1/public/configs [get]
func (c *SystemController) GetPublicConfigs(ctx *gin.Context) {
	configs, err := c.configService.GetPublicConfigs()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, configs)
}

// GetAuditLogs 获取审计日志列表
// @Summary 获取审计日志列表
// @Description 获取审计日志列表（分页）
// @Tags 系统管理
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "页码" default(1)
// @Param page_size query int false "每页数量" default(10)
// @Param keyword query string false "搜索关键词"
// @Param user_id query string false "用户ID"
// @Param username query string false "用户名"
// @Param action query string false "操作动作"
// @Param resource query string false "资源类型"
// @Param method query string false "请求方法"
// @Param status query string false "响应状态"
// @Param ip query string false "IP地址"
// @Param start_date query string false "开始日期"
// @Param end_date query string false "结束日期"
// @Success 200 {object} model.Response{data=model.PaginationResponse}
// @Failure 400 {object} model.Response
// @Router /api/v1/system/audit-logs [get]
func (c *SystemController) GetAuditLogs(ctx *gin.Context) {
	var req model.AuditLogSearchRequest
	if err := utils.BindQueryAndValidate(ctx, &req); err != nil {
		return
	}

	// 设置默认值
	if req.Page <= 0 {
		req.Page = 1
	}
	if req.PageSize <= 0 {
		req.PageSize = 10
	}

	resp, err := c.auditService.GetAuditLogList(&req)
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, resp)
}

// GetAuditLog 获取审计日志详情
// @Summary 获取审计日志详情
// @Description 根据ID获取审计日志详情
// @Tags 系统管理
// @Produce json
// @Security ApiKeyAuth
// @Param id path int true "日志ID"
// @Success 200 {object} model.Response{data=model.AuditLog}
// @Failure 400 {object} model.Response
// @Router /api/v1/system/audit-logs/{id} [get]
func (c *SystemController) GetAuditLog(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		utils.BadRequestResponse(ctx, "无效的日志ID")
		return
	}

	log, err := c.auditService.GetAuditLogByID(uint(id))
	if err != nil {
		utils.NotFoundResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, log)
}

// GetAuditLogStats 获取审计日志统计
// @Summary 获取审计日志统计
// @Description 获取审计日志统计信息
// @Tags 系统管理
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Failure 500 {object} model.Response
// @Router /api/v1/system/audit-logs/stats [get]
func (c *SystemController) GetAuditLogStats(ctx *gin.Context) {
	stats, err := c.auditService.GetAuditLogStats()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, stats)
}

// CleanupAuditLogs 清理审计日志
// @Summary 清理审计日志
// @Description 清理指定天数之前的审计日志
// @Tags 系统管理
// @Produce json
// @Security ApiKeyAuth
// @Param days query int true "保留天数"
// @Success 200 {object} model.Response
// @Failure 400 {object} model.Response
// @Router /api/v1/system/audit-logs/cleanup [post]
func (c *SystemController) CleanupAuditLogs(ctx *gin.Context) {
	daysStr := ctx.Query("days")
	if daysStr == "" {
		utils.BadRequestResponse(ctx, "保留天数不能为空")
		return
	}

	days, err := strconv.Atoi(daysStr)
	if err != nil || days <= 0 {
		utils.BadRequestResponse(ctx, "保留天数必须是正整数")
		return
	}

	if err := c.auditService.CleanupOldLogs(days); err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	utils.SuccessResponse(ctx, nil)
}

// GetSystemInfo 获取系统信息
// @Summary 获取系统信息
// @Description 获取系统基本信息
// @Tags 系统管理
// @Produce json
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Router /api/v1/public/system/info [get]
func (c *SystemController) GetSystemInfo(ctx *gin.Context) {
	// 获取公开配置
	configs, err := c.configService.GetPublicConfigs()
	if err != nil {
		utils.ServerErrorResponse(ctx, err.Error())
		return
	}

	// 构建系统信息
	systemInfo := make(map[string]interface{})
	for _, config := range configs {
		if config.Key == "system.name" {
			systemInfo["name"] = config.Value.GetString("value")
		} else if config.Key == "system.version" {
			systemInfo["version"] = config.Value.GetString("value")
		}
	}

	// 添加其他系统信息
	systemInfo["description"] = "VulnArk漏洞管理系统"
	systemInfo["author"] = "VulnArk Team"

	utils.SuccessResponse(ctx, systemInfo)
}

// GetClientConfig 获取客户端配置
// @Summary 获取客户端配置
// @Description 获取前端客户端需要的配置信息
// @Tags 系统管理
// @Produce json
// @Success 200 {object} model.Response{data=map[string]interface{}}
// @Router /api/v1/public/client-config [get]
func (c *SystemController) GetClientConfig(ctx *gin.Context) {
	// 获取当前请求的Host信息
	host := ctx.Request.Host
	scheme := "http"
	if ctx.Request.TLS != nil {
		scheme = "https"
	}

	// 如果是通过代理访问，尝试获取原始Host
	if forwardedHost := ctx.GetHeader("X-Forwarded-Host"); forwardedHost != "" {
		host = forwardedHost
	}
	if forwardedProto := ctx.GetHeader("X-Forwarded-Proto"); forwardedProto != "" {
		scheme = forwardedProto
	}

	clientConfig := map[string]interface{}{
		"apiBaseURL": fmt.Sprintf("%s://%s/api/v1", scheme, host),
		"version":    "1.0.0",
		"appName":    "VulnArk",
		"features": map[string]bool{
			"aiAssistant":    true,
			"fileUpload":     true,
			"notifications":  true,
			"reports":        true,
		},
	}

	utils.SuccessResponse(ctx, clientConfig)
}
